
(ns mlib.fb.webhook
  (:import
    [javax.crypto Mac]
    [javax.crypto.spec SecretKeySpec])
  (:require
    [clojure.core.async :refer [chan >!!]]
    [clojure.string :as s]
    [cheshire.core :as json]
    [ring.adapter.jetty :refer [run-jetty]]
    [ring.middleware.params :refer [wrap-params]]
    [mount.core :refer [defstate]]

    [mlib.log :refer [debug info warn]]
    [mlib.core :refer [hexbyte]]
    [mlib.conf :refer [conf]]))
;

(defn resp [status body]
  {:status status :headers {"Content-Type" "text/plain"} :body body})
;

;;; ;;; ;;;

(defn process-post [sink-fn ctx params]
  (if (= "page" (:object params))
    (doseq [entry (:entry params)
            :let [page-id (:id entry) time (:time entry)]]
      (doseq [msg (:messaging entry)]
        (sink-fn [ctx msg])))
    ;
    (warn "unexpected object:" params))
  ;
  (resp 200 "OK"))
;

(defn page-verify [verify-token params]
  (if (and
        (= "subscribe"  (get params "hub.mode"))
        (= verify-token (get params "hub.verify_token")))
    (resp 200 (get params "hub.challenge"))
    (resp 403 "hub.verify error")))
;

;;; ;;; ;;;

(defn hmac [key data]
  (let [mac (Mac/getInstance "HmacSHA1")]
    (.init mac (SecretKeySpec. (.getBytes key "UTF-8") "HmacSHA1"))
    (->>
      (.doFinal mac (.getBytes data "UTF-8"))
      (map hexbyte)
      (apply str))))
;

;; https://github.com/ring-clojure/ring/tree/1.5.0/ring-core/src/ring/middleware

(defn- keyword-syntax? [s]
  (re-matches #"[A-Za-z*+!_?-][A-Za-z0-9*+!_?-]*" s))

(defn- keyify-params [target]
  (cond
    (map? target)
    (into {}
      (for [[k v] target]
        [(if (and (string? k) (keyword-syntax? k))
           (keyword k)
           k)
         (keyify-params v)]))
    ;
    (vector? target)
    (vec (map keyify-params target))
    ;
    :else
    target))
;

;;;;

(defn signed-json-body [handler app-secret headers body]
  (let [[_ x-sig] (s/split (str (get headers "x-hub-signature")) #"=")
        raw-body (slurp body)]
    (if (= x-sig (hmac app-secret raw-body))
      (handler 
        (-> raw-body (json/parse-string true) keyify-params))
      (do
        (warn "x-hub-signature mismatch:" x-sig)
        (resp 403 "x-hub-signature mismatch")))))
;

(defn get-or-post [sink-fn req]
  ; [id (last (s/split (:uri req) #"/"))] ;; endpoint->app
  (let [fb (:fb conf)
        ctx (select-keys fb [:app-id :page-token])]
    (case (:request-method req)
      :get  
        (page-verify 
          (:verify-token fb)
          (:params req)) 
      :post 
        (signed-json-body 
          (partial process-post sink-fn ctx) 
          (:app-secret fb)
          (:headers req)
          (:body req))
      (do
        (warn "unexpected method:" req)
        (resp 403 "unexpected method")))))
;

(defn wrap-syserr [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (do
          (warn "webhook:" e)
          (resp 500 "internal error"))))))
;

;;; ;;; ;;;

(defrecord Webhook [listener chan])

(defstate webhook
  :start
    (if-let [cnf (-> conf :fb :listener)]
      (let [sink-chan (chan)
            handler (-> 
                      (partial get-or-post #(>!! sink-chan %)) 
                      (wrap-params) 
                      (wrap-syserr))]
        (info "webhook starting")
        (Webhook.
          (run-jetty handler cnf)
          sink-chan))
      ;;
      (do
        (warn "webhook disabled.")
        false))
  :stop
    (when webhook
      (info "webhook stopping")
      (.stop (:listener webhook))))
;

;;.
