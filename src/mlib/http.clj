
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

(defn json-resp [body]
  { :status  200
    :headers {"Content-Type" "application/json;charset=utf-8"}
    :body    (generate-string body)})
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
