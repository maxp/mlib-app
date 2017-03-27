
(ns mlib.http
  (:require
    [cheshire.core :refer [generate-string]]
    [cheshire.generate :refer [add-encoder]]))
;

(defn json-str [obj]
  (generate-string obj))
;

(defn text-resp [body]
  { :status  200
    :headers {"Content-Type" "text/plain;charset=utf-8"}
    :body    (str body)})
;

(defn html-resp [body]
  { :status  200
    :headers {"Content-Type" "text/html;charset=utf-8"}
    :body    (str body)})
;

(defn json-resp
  ( [body]
    { :status  200
      :headers {"Content-Type" "application/json;charset=utf-8"}
      :body    (generate-string body)})
  ( [status body]
    { :status  status
      :headers {"Content-Type" "application/json;charset=utf-8"}
      :body    (generate-string body)}))
;

; 400 Bad Request
; 402 Payment Required
; 401 Unauthorized
; 403 Forbidden
; 404 Not Found
; 405 Method Not Allowed
; 429 Too Many Requests
;
; 500 Internal Server Error
; 501 Not Implemented
; 502 Bad Gateway
; 503 Service Unavailable

(defn json-err [body]
  (json-resp 400 body))
;

(defn json-syserr [body]
  (json-resp 500 body))
;

(defn json-request?
  "check for application/json content-type"
  [req]
  (if-let [ctype (get-in req [:headers "content-type"])]
    (not (empty? (re-find #"^application/(.+\+)?json" ctype)))))
;

(defn ajax? [request]
  (= "XMLHttpRequest" (get-in request [:headers "x-requested-with"])))
;

(defn make-url [sheme host uri qs]
  (str sheme "://" host uri (and qs (str "?" qs))))
;

(add-encoder org.joda.time.DateTime
  (fn [c jsonGenerator]
    (.writeString jsonGenerator (str c))))
;

;;.
