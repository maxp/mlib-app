
(ns mlib.mdb.conn
  (:require
    ;[taoensso.timbre :refer [warn]]
    ;[clj-time.core :as t]
    [mount.core :refer [defstate]]
    [monger.json]
    [monger.joda-time]
    [monger.core :as mg]
    [mlib.conf :refer [conf]])
  (:import
    [org.joda.time DateTimeZone]
    [com.mongodb WriteConcern]))
;


(defn connect [cnf]
  (->
    (:tz conf "Asia/Irkutsk")
    DateTimeZone/forID
    DateTimeZone/setDefault)
  (let [mdb (mg/connect-via-uri (:uri cnf))]
    (mg/set-default-write-concern! WriteConcern/FSYNC_SAFE)
    mdb))
;

(defn disconnect [mdb]
  (mg/disconnect (:conn mdb)))
;

(defstate mdb
  :start
    (connect (-> conf :mdb :angara))
  :stop
    (disconnect mdb))
;

(defn dbc []
  (:db mdb))
;

;;.
