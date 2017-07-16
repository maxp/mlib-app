
(ns mlib.bcrypt
  (:import
    [org.mindrot.jbcrypt BCrypt])
  (:require
    [clojure.tools.logging :refer [warn]]))
;


; http://www.mindrot.org/projects/jBCrypt/

(defn check-pass [password hash]
  (try
    (BCrypt/checkpw password hash)
    (catch Exception e
      (warn "check-pass:" (.getMessage e)))))
;

(defn hash-pass [password]
  (try
    (BCrypt/hashpw password (BCrypt/gensalt))
    (catch Exception e
      (warn "hash-pass:" (.getMessage e)))))
;

;;.
