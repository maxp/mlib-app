
;; TODO: update library source

(ns mlib.log
  (:require
    [clojure.tools.logging]))
;

;;;;;; log macro ;;;;;;

;; <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %level %logger{16} %msg%n</pattern>

(defn- source-line [frm]
  (:line (meta frm)))
;

(defmacro debug [message & args]
  `(clojure.tools.logging/logp :debug
      (str ~(source-line &form) ": " ~message)
      ~@args))
;

(defmacro info [message & args]
  `(clojure.tools.logging/logp :info
      ~message ~@args))
;

(defmacro warn [message & args]
  `(clojure.tools.logging/logp :warn
      ~message ~@args))
;

(defmacro error [message & args]
  `(clojure.tools.logging/logp :error
      ~message ~@args))
;

(comment
  (debug "!debug!" 1 2 3)
  (info  "!info!" 1 2 3)
  (warn  "!warn!" 1 2 3))
;


;;;;;; try macro ;;;;;;

(defmacro try-warn [label & body]
  `(try ~@body
    (catch Exception e#
      (warn ~label e#))))
;
