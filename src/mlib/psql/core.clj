
(ns mlib.psql.core
  (:require
    [clojure.string :as s]
    [jdbc.core :as jdbc]
    [honeysql.core :as sql]
    [mlib.log :refer [warn]]
    [mlib.psql.conn :refer [dbc]]))
;


(defn fetch [sqlmap & [params]]
  (try
    (with-open [conn (dbc)]
      (jdbc/fetch conn
        (sql/format sqlmap :params params)))
    (catch Exception e
      (warn "fetch:" e))))
;

(defn fetch-one [sqlmap & [params]]
  (try
    (first
      (with-open [conn (dbc)]
        (jdbc/fetch conn
          (sql/format sqlmap :params params))))
    (catch Exception e
      (warn "fetch-one:" e))))
;

(defn exec [sqlmap & [params]]
  (try
    (with-open [conn (dbc)]
      (jdbc/execute conn
        (sql/format sqlmap :params params)))
    (catch Exception e (warn e))))
;

(defn next-id [tbl]
  (let [sel-next (str "select nextval('" tbl "_id_seq')")]
    (try
      (:nextval
        (first
          (with-open [conn (dbc)]
            (jdbc/fetch conn [sel-next]))))
      (catch Exception e
        (warn "next-id:" tbl e)))))
;

(defn insert-into [tbl data]
  (let [fld-pairs (seq data)]
    (= 1 (exec {:insert-into (keyword tbl)
                :columns (map first fld-pairs)
                :values [(map second fld-pairs)]}))))
;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(comment
  (def struct
    { :table "table_name"
      :fields
        [ :id [:serial :pkey]
          :name1 "varchar"
          "name2" :varchar
          :ts  [:timestamp :now :null]]
      :indexes
        ["ts"]
      :unique
        ["uniq,flds"]}))
;

(defn field-type [type]
  (let
    [ null-flag
        (atom false)
      shortcuts
        (fn [t]
          (case t
            :pkey      "primary key"
            :bool      "boolean"
            :str40     "varchar(40)"
            :str80     "varchar(80)"
            :id        "varchar(40)"    ;; generic ID type
            :ts        "timestamp with time zone default CURRENT_TIMESTAMP"
            :timestamp "timestamp with time zone"
            :now       "default CURRENT_TIMESTAMP"
            :zero      "default 0"
            :true      "default 't'"
            :false     "default 'f'"
            :money     "numeric(18,2)"
            :amount    "numeric(18,3)"
            :nullable  (do (reset! null-flag true) nil)
            :null      (do (reset! null-flag true) nil)
            (name t)))
        ;
      ft
        (cond
          (keyword? type)
          (name type)
          ;
          (string? type)
          type
          ;
          (vector? type)
          (s/join " " (for [t type] (shortcuts t)))
          ;
          :else (str type))]
    ;
    (str ft (when-not @null-flag " not null"))))
;

(defn create-table-sql [struct]
  (str
    "create table " (-> struct :table name) " ("
    (s/join ", "
      (for [[fld type] (partition 2 2 nil (:fields struct))]
        (str (name fld) " " (field-type type))))
    ")"))
;

(defn create-index-sql [modif table idx ord]
  (str "create " modif " index " table "_idx" (inc ord)
        " on " table "(" (name idx) ")"))

;;; ;;;

(defn create-table [struct]
  (with-open [conn (dbc)]
    (jdbc/execute conn
      (create-table-sql struct))
    ;
    (let [ord (atom 0)
          next-ord #(swap! ord inc)
          tbl (-> struct :table name)]
      ;
      (doseq [idx (:unique struct)]
        (jdbc/execute conn
          (create-index-sql "unique" tbl idx (next-ord))))
      ;
      (doseq [idx (:indexes struct)]
        (jdbc/execute conn
          (create-index-sql "" tbl idx (next-ord)))))))
  ;
;

(defn drop-table [struct]
  (with-open [conn (dbc)]
    (jdbc/execute conn (str "drop table " (:table struct)))))
;

;;.
