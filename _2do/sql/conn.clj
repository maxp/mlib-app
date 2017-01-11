
(ns mlib.sql.conn
  ; (:import
  ;   com.mchange.v2.c3p0.ComboPooledDataSource)
  (:require
    [taoensso.timbre :refer [info warn]]
    [clj-time.coerce :as tc]
    [jdbc.core :as jdbc]
    [jdbc.proto :refer [ISQLType ISQLResultSetReadColumn]]
    [hikari-cp.core :refer [make-datasource]]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]))
;

; (defn make-pool [conf]
;   (doto
;     (ComboPooledDataSource.)
;     (.setDriverClass "org.postgresql.Driver")
;     (.setJdbcUrl (:jdbc conf))
;     (.setUser (:user conf))
;     (.setPassword (:password conf))
;     ;; expire excess connections after 30 minutes of inactivity:
;     (.setMaxIdleTimeExcessConnections (* 30 60))
;     ;; expire connections after 3 hours of inactivity:
;     (.setMaxIdleTime (* 3 60 60))
;     (.setMaxPoolSize 8)))
; ;

; http://www.mchange.com/projects/c3p0/
      ; .setMinPoolSize(5);
      ; .setAcquireIncrement(5);
      ; .setMaxPoolSize(20))});
      ; acquireIncrement
      ; initialPoolSize
      ; maxPoolSize
      ; maxIdleTime
      ; minPoolSize)})
;


;; https://github.com/tomekw/hikari-cp/
(comment
  (make-datasource
         {:connection-timeout 30000
          :idle-timeout 600000
          :max-lifetime 1800000
          :minimum-idle 10
          :maximum-pool-size  10
          :adapter "postgresql"
          :username "username"
          :password "password"
          :database-name "database"
          :server-name "localhost"
          :port-number 5432})

  (with-open [conn (jdbc/connection ds)]
    (do-stuff conn)))
;


(defstate ds
  :start
    (make-datasource (:psql conf))
  :stop
    (.close ds))
;

(defn dbc []
  (jdbc/connection ds))
;

;;; ;;; ;;; ;;;

(extend-protocol ISQLType
  ;
  org.joda.time.DateTime
  (as-sql-type [this conn]
    (tc/to-sql-time this))
  ;
  (set-stmt-parameter! [this conn stmt index]
    (.setTimestamp stmt index
      (tc/to-sql-time this))))
;

(extend-protocol ISQLResultSetReadColumn
  ;
  java.sql.Timestamp
  (from-sql-type [this conn metadata index]
    (tc/from-sql-time this))
  ;
  java.sql.Date
  (from-sql-type [this conn metadata index]
    (tc/from-sql-date this))
  ;
  java.sql.Time
  (from-sql-type [this conn metadata index]
    (org.joda.time.DateTime. this)))
;

; ; http://clojure.github.io/java.jdbc/#clojure.java.jdbc/IResultSetReadColumn
; (extend-protocol jdbc/IResultSetReadColumn
;   java.sql.Timestamp
;   (result-set-read-column [v _2 _3]
;     (tc/from-sql-time v))
;   java.sql.Date
;   (result-set-read-column [v _2 _3]
;     (tc/from-sql-date v))
;   java.sql.Time
;   (result-set-read-column [v _2 _3]
;     (org.joda.time.DateTime. v)))
;
; ; http://clojure.github.io/java.jdbc/#clojure.java.jdbc/ISQLValue
; (extend-protocol jdbc/ISQLValue
;   org.joda.time.DateTime
;   (sql-value [v]
;     (tc/to-sql-time v)))

;;.