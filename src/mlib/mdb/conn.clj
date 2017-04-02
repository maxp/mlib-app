
(ns mlib.mdb.conn
  (:require
    [monger.json]
    [monger.joda-time]
    [monger.core :as mg]
    ; [mount.core :refer [defstate]]
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

;(defstate mdb
;  :start
;    (connect (-> conf :mdb))
;  :stop
;    (disconnect mdb))
;

;(defn dbc []
;  (:db mdb))
;

;;.
