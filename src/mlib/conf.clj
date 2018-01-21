
(ns mlib.conf
  (:require
    [mount.core :refer [defstate args]]
    [mlib.core :refer [deep-merge edn-resource]]))
;

(defstate conf
  :start
    (deep-merge
      (edn-resource "config.edn")
      (args)))
;

;.
