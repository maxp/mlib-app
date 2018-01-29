
(ns mlib.tg.poller
  (:require
    [clojure.core.async :refer [>!! chan]]
    [mlib.log :refer [debug info warn]]
    [mlib.core :refer [to-int]]
    [mlib.tg.core :refer [api]]))
;

(def POLL_LIMIT 100)
(def POLL_TIMEOUT 5)  ;; seconds
(def POLL_ERROR_SLEEP 3000)

(defn update-loop [run-flag cnf msg-chan]
  (let [token (:apikey cnf)
        poll-limit (:poll-limit cnf POLL_LIMIT)
        poll-timeout (:poll-timeout cnf POLL_TIMEOUT)
        poll-error-sleep (:poll-error-sleep cnf POLL_ERROR_SLEEP)]
    ;
    (debug "poller loop begin.")
    ;
    (loop [last-id 0  updates nil]
      (if @run-flag
        (if-let [u (first updates)]
          (let [id (-> u :update_id to-int)]
            (if (< last-id id)
              (when-not (>!! msg-chan u)
                (info "poller loop: msg-chan closed.")
                (reset! run-flag false))
              (debug "update-dupe:" id))
            (recur id (next updates)))
          ;
          (let [upd (api token :getUpdates
                      { :offset (inc last-id)
                        :limit poll-limit
                        :timeout poll-timeout})
                {err :error} upd]
            (if err
              (do
                (warn "api-error:" upd)
                (Thread/sleep poll-error-sleep)
                (recur last-id nil))
              (recur last-id upd))))
        ;;
        (debug "poller loop end.")))))
;


(defn start [cnf out-chan]
  (let [run-flag (atom true)
        bot-name (:name cnf)]
    (if cnf
      { :thread   (-> #(update-loop run-flag cnf out-chan) Thread. .start)
        :run-flag run-flag
        :chan     out-chan
        :name     bot-name}
      ;
      (do
        (warn "poller disabled in config:" bot-name)
        false))))
;

(defn stop [poller]
  (debug "poller stop:" (:name poller))
  (reset! (:run-flag poller) false))
;

(defn get-chan [poller]
  (:chan poller))
;

;;.
