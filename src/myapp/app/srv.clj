(ns myapp.app.srv
  (:require
    [clojure.string :as s]
    [clj-time.core :as tc]
    [clojure.core.async :refer [chan <!! alts!! close! thread timeout]]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]
    [mlib.log :refer [debug info warn]]))
;


(defn handler [ch]
  (prn "ch:" ch) 
  (let [tout (timeout 5000)
        [d c] (alts!! [ch tout])]
    (prn "data:" d)
    (if (= c tout)
      ch        ; continue
      nil)))    ; exit loop
;


(defn thread-loop 
  "loop-fn receives old state and returns new, loop stops whna new state is falsy"
  [loop-fn initial-state]
  (thread 
    (loop [state initial-state]
      (when-let [new-state (loop-fn state)]
        (recur new-state)))
    (info "thread-loop: end.")))
;


(defstate worker
  :start
    (let [ch (chan)]
      (debug "start worker")
      { :chan ch
        :thread (thread-loop handler ch)})
  :stop
    (when worker
      (debug "stop worker")
      (close! (:chan worker))))
;

;;.

