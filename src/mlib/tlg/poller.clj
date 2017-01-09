
(ns mlib.tlg.poller
  (:require
    [clojure.core.async :refer [>!! chan]]
    [taoensso.timbre :refer [debug info warn]]
    [mlib.core :refer [to-int]]
    [mlib.tlg.core :refer [api]]))
;


(defn update-loop [run-flag cnf msg-chan]
  (let [token (:apikey cnf)
        poll-limit (:poll-limit cnf 100)
        poll-timeout (:poll-timeout cnf 1)
        api-error-sleep (:api-error-sleep cnf 3000)]
    ;
    (reset! run-flag true)
    (debug "update-loop started.")
    ;
    (loop [last-id 0  updates nil]
      (if @run-flag
        (if-let [u (first updates)]
          (let [id (-> u :update_id to-int)]
            (if (< last-id id)
              (when-not (>!! msg-chan u)
                (info "msg-chan closed! exiting loop")
                (reset! run-flag false))
              (debug "update-dupe:" id))
            (recur id (next updates)))
          ;
          (let [upd (api token :getUpdates
                      { :offset (inc last-id)
                        :limit poll-limit
                        :timeout poll-timeout})]
            (when-not upd
              (warn "api-error")
              (Thread/sleep api-error-sleep))
            (recur last-id upd)))
        ;;
        (debug "update-loop stopped.")))))
;


(defn start [bot-conf & [out-chan]]
  (let [out-chan (or out-chan (chan))
        run-flag (atom nil)
        bot-name (:name bot-conf)]
    (if (-> bot-conf :apikey not-empty)
      { :thread
          (-> #(update-loop run-flag bot-conf out-chan) Thread. .start)
        :run-flag run-flag
        :chan out-chan
        :name bot-name}
      ;
      (warn "bot start disabled in config:" bot-name))))
;

(defn stop [poller]
  (debug "stopping poller:" (:name poller))
  (reset! (:run-flag poller) false))
;

(defn get-chan [poller]
  (:chan poller))
;



;;.
