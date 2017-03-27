;
; depends on cookies middleware
;

(ns mlib.web.sess
  (:require
    [ring.util.response :refer [get-header set-cookie]]))
;

(def SESS_COOKIE "sid")
(def SESS_MAXAGE (* 365 24 3600))
(def SESS_PATH   "/")


(defn sid-resp [resp sid domain tmp]
  (let [data {:value sid
              :path SESS_PATH
              :http-only true}
        data (if tmp
                data
                (assoc data :max-age SESS_MAXAGE))
        data (if domain
                (assoc data :domain domain)
                data)]
    (update-in resp [:cookies]
      #(assoc % SESS_COOKIE data))))
;

(defn wrap-sess [handler sess-load]
  (fn [req]
    (if-let [sid (get-in req [:cookies SESS_COOKIE :value])]
      (handler (assoc req :sess (sess-load sid)))
      (handler req))))
;

;;.
