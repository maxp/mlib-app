
(ns app.main
  (:gen-class)
  (:require
    [mount.core :refer [start-with-args]]
    [mlib.conf :refer [conf]]
    [mlib.core :refer [edn-read]]
    [mlib.log :refer [info warn]]
    [app.srv]))
;

(defn -main [& args]
  (if-let [rc (edn-read (first args))]
    (start-with-args rc)
    (warn "config profile must be in parameters!")))
  ;

;;.
