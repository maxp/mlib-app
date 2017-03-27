
(ns mlib.psql.core
  (:require
    [clojure.java.jdbc :as jdbc]
    [mount.core :refer [defstate]]
    [mlib.log :refer [warn]]
    [mlib.conf :refer [conf]])
  (:import
    com.mchange.v2.c3p0.ComboPooledDataSource))
;

(defn make-pool [conf]
  {:datasource
    (doto
      (ComboPooledDataSource.)
      (.setDriverClass "org.postgresql.Driver")
      (.setJdbcUrl (:jdbc conf))
      (.setUser (:user conf))
      (.setPassword (:password conf))
      ;; expire excess connections after 30 minutes of inactivity:
      (.setMaxIdleTimeExcessConnections (* 30 60))
      ;; expire connections after 3 hours of inactivity:
      (.setMaxIdleTime (* 3 60 60)))})

; http://www.mchange.com/projects/c3p0/
      ; .setMinPoolSize(5);
      ; .setAcquireIncrement(5);
      ; .setMaxPoolSize(20))});
      ; acquireIncrement
      ; initialPoolSize
      ; maxPoolSize
      ; maxIdleTime
      ; minPoolSize)})


(defstate ds
  :start (make-pool (:psql conf))
  :stop (.close (:datasource ds)))

;;.
