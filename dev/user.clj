
(ns user
  (:require
    [clojure.tools.namespace.repl :as repl]
    [clojure.edn :as edn]
    [mount.core :as mount]))
;

(defn start []
  (require 'bots.main)
  (mount/start-with-args
    (-> "dev/conf.edn" slurp edn/read-string)))
;

(defn go []
  (mount/stop)
  (repl/refresh :after 'user/start))
;

(defn reset []
  (prn "reset.")
  (go))
;

;;.
