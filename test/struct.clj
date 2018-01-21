
(ns skern.db.struct
  (:require
    [jdbc.core :as jdbc]
    [taoensso.timbre :refer [info warn]]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]
    [skern.db.conn :refer [dbc]]
    [skern.db.sql :refer [create-table]]))
;

;; common fields
(defn cflds [& fld-pairs]
  (concat
    [ :id [:serial :pkey]
      :ct [:timestamp :now]
      :ts [:timestamp :now]
      :status [:int :zero]]
    fld-pairs))
;

(def TOKENS
  { :table (keyword "tokens")
    :fields
      [
        :id [:varchar :pkey]
        :ct [:timestamp :now]
        :ts [:timestamp :now]
        :valid [:boolean :true]
        :data [:varchar]] ;; :nullable
    :indexes
      [:ts]})
;

;;; ;;; ;;; ;;; ;;;

(def PUBLISHERS
  { :table (keyword "publishers")
    :fields
      [
        :id [:serial :pkey]
        :ct [:timestamp :now]
        :ts [:timestamp :now]
        :status [:int :zero]
        :email    :varchar
        :passhash :varchar
        :name     :varchar
        :props    :varchar
        :info     :varchar]
    :unique
      ["lower(email)"]})
;

; (nadya.db.sql/create-table nadya.db.struct/PUBLISHERS)

;;; ;;; ;;; ;;; ;;;

(def COURSES
  { :table (keyword "courses")
    :fields
      (cflds
        :publisher_id [:int :zero]
        :name [:varchar]
        :daynum [:int :zero]
        :prereq [:varchar] ;; space separates list of course_id
        :props [:varchar]
        :info [:varchar])
    :indexes
      [:publisher_id]})
;

;;; ;;; ;;; ;;; ;;;

(def COURSEDAYS
  { :table (keyword "coursedays")
    :fields
      (cflds
        :course_id  [:int]        ;; references courses(id)
        :day        [:int]        ;; 1-based
        :data       [:varchar])   ;; json
    :indexes
      [:course_id]
    :unique
      ["course_id,day"]})
;


(comment
  (create-table COURSES)
  (create-table COURSEDAYS))
;

;;; ;;; ;;; ;;; ;;;

(def USERS
  { :table (keyword "users")
    :fields
      [ :id [:str40 :pkey]        ;; facebook user-id
        :ct [:timestamp]
        :ts [:timestamp]
        :state    [:varchar :null]        ;; json
        :props    [:varchar :null]        ;; json
        :settings [:varchar :null]]})     ;; json
;

(comment
  (create-table USERS))

;;; ;;; ;;; ;;; ;;;

(def TIMESLOTS
  { :table (keyword "timeslots")
    :fields
      (cflds
        ;; status: -1: deleted, 0: none, 1: seen, 2: failed
        :sheduled   [:timestamp]
        ;
        :course_id  [:int]
        :day        [:int]
        :user_id    [:str80])
        ;; fin ?
    :indexes
      [:sheduled :user_id]})
;

;; -- is attended by user: (cnt > 0)
;; select count(*) as cnt from timeslots
;; where user_id=? and course=? and status=1 and fin;
;;
;; -- user attended courses:
;; select distinct course fron timeslots where user_id=? and state=1 and fin;

(defstate tables
  :start
    (when (:create-tables conf)
      (doseq [t [TOKENS PUBLISHERS COURSES COURSEDAYS USERS TIMESLOTS]]
        (info "create table:" (:table t))
        (try
          (create-table t)
          (catch Exception e
            (warn "create-table:" e))))))
;

;;.
