
(ns mlib.mdb.core
  (:require
    [taoensso.timbre :refer [warn]]
    [clj-time.core :as t]
    [mount.core :refer [defstate]]
    [monger.json]
    [monger.joda-time]
    [monger.core :refer
      [connect-via-uri disconnect set-default-write-concern!]]
    [monger.collection :refer
      [find-and-modify ensure-index]]
    [monger.query :as query]
    [mlib.conf :refer [conf]])
  (:import
    [org.bson.types ObjectId]
    [com.mongodb WriteConcern]
    [org.joda.time DateTimeZone]))
;

;;; ;;; _id util ;;; ;;;

(defn new_id [] (ObjectId.))

(defn oid [s]
  (try (ObjectId. s) (catch Exception e (str s))))

(defn id_id [r]
  (if-let [id (:_id r)]
    (assoc (dissoc r :_id) :id id)
    r))
;

;;; ;;; connection ;;; ;;;

(defn set-default-timezone []
  (-> (:tz conf "Asia/Irkutsk") DateTimeZone/forID DateTimeZone/setDefault))
;

(defn connect [cnf]
  (let [conn-db (connect-via-uri (:uri cnf))]
    (set-default-write-concern! WriteConcern/FSYNC_SAFE)
    conn-db))
;

(defn indexes [db]
  (when db))
    ; (ensure-index db user-coll {:auth 1})
    ; (ensure-index db story-coll (array-map :locs 1 :ct 1))
    ; (ensure-index db story-coll (array-map :ts 1))
;

(defstate mdb
  :start
    (let [_       (set-default-timezone)
          conn-db (connect (:mdb conf))
          _       (indexes (:db conn-db))]
      conn-db)
  :stop
    (disconnect (:conn mdb)))
;

(defn dbc []
  (:db mdb))

;;; ;;; serials ;;; ;;;

(def SERIALS "serials")

(defn next-serial [serial-name]
  (try
    (-> (find-and-modify (dbc) SERIALS
          {:_id (name serial-name)}
          {"$inc" {:n (int 1)}}
          {:return-new true :upsert true})
      :n long)
    (catch Exception e (warn "mdb/next-serial:" e))))
;

(defn next-sn [serial-name]
  (try
    (-> (find-and-modify (dbc) SERIALS
          {:_id (name serial-name)}
          {"$inc" {:n (int 1)}}
          {:return-new true :upsert true})
      :n str)
    (catch Exception e (warn "mdb/next-sn:" e))))

;;; ;;; ;;; ;;;

;;.
