
(ns mlib.fb.api
  (:require
    [clj-http.client :as http]
    [cheshire.core :as json]    
    [mlib.log :refer [info warn debug]]
    [mlib.conf :refer [conf]]))
;


(def API_RETRY 3)

(defn api-timeout []
  (or
    (get-in conf [:fb :api-timeout])
    30000))
;

;; ptok - page_access_token

(defn call-api [method-fn ptok node params query-params]
  (try
    (let [tout (api-timeout)
          res (method-fn (str "https://graph.facebook.com/v2.6/" node)
                  { :query-params (merge {:access_token ptok} query-params)
                    :content-type :json
                    :throw-exceptions false
                    :form-params params
                    :socket-timeout tout
                    :conn-timeout tout
                    :accept :json
                    :retry-handler  (fn [ex try-count http-context]
                                      (if (> try-count API_RETRY) false true))})]                    
                                    ;; java.net.SocketTimeoutException
      (json/parse-string (:body res) true))
    (catch Exception e
      (warn "call-api:" (.getMessage e))
      {:error "connection"})))
;


(defn api-get [ptok node params & [query-params]]
  (call-api http/get ptok node params query-params))

(defn api-post [ptok node params & [query-params]]
  (call-api http/post ptok node params query-params))

  
(defn send-message [ptok data]
  (api-post ptok "me/messages" data))
;

  ;;
  ; ( [token endpoint data]
  ;   (let [tout (api-timeout)]
  ;     (try
  ;       (:body
  ;         (http/post (str "https://graph.facebook.com/v2.6/me/" endpoint)
  ;          { :query-params {:access_token token}
  ;            :content-type :json
  ;            :throw-exceptions false
  ;            :form-params data
  ;            :socket-timeout tout
  ;            :conn-timeout tout
  ;            :accept :json
  ;            :as :json}))
  ;       (catch Exception e
  ;         (warn "send-message:" endpoint e))))))
;


;; max size - 10Mb
;; https://github.com/dakrone/clj-http#exceptions
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference

(defn send-file
  [page-token user-id type mime-type file]
  (try
    (let [tout (api-timeout)]
      (->
        (http/post
          (str "https://graph.facebook.com/v2.6/me/messages?access_token="
                page-token)
          { :multipart
            [ { :name "recipient"
                :content (str "{\"id\":\"" user-id "\"}")}
              { :name "message"
                :content (str "{\"attachment\":{\"type\":\"" type
                              "\",\"payload\":{}}}")}
              { :name "filedata" :mime-type mime-type :content file}]
            :socket-timeout tout
            :conn-timeout tout
            :as :json})
        (:body)
        (:message_id)))
    (catch Exception e
      (warn "send-file:" user-id e))))
;


(def media-mime-type-map
  {
    :gif   ["image" "image/gif"]
    :png   ["image" "image/png"]
    :jpg   ["image" "image/jpeg"]
    :jpeg  ["image" "image/jpeg"]
    :audio ["audio" "audio/mp3"]
    :mp3   ["audio" "audio/mp3"]
    :video ["video" "video/mp4"]
    :mp4   ["video" "video/mp4"]})
;

(defn send-media
  [page-token user-id type file]
  (if-let [[t m] (media-mime-type-map type)]
    (send-file page-token user-id t m file)
    (warn "send-media: unsupported media attachment type - " type user-id)))
;

; (defn test-send-file []
;   (send-media
;     (-> conf :fb :pages (get 0) :token)
;     "1141287622608_11"
;     :jpeg
;     (clojure.java.io/file "/tmp/red.jpg")))
; ;


; (defn send-text [ptok user-id text]
;   (send-message ptok {:recipient {:id user-id} :message {:text text}}))
;


(defn user-profile [ptok user-id & [params]]
  (let [tout (api-timeout)]
    (try
      (:body
        (http/get (str "https://graph.facebook.com/v2.6/" user-id)
          { :query-params (merge params {:access_token ptok})
            :socket-timeout tout
            :conn-timeout tout
            :accept :json}
          :as :json))
      (catch Exception e
        (warn "user-profile:" e)))))
;


;
; curl  \
;   -F recipient='{"id":"${USER_ID}"}' \
;   -F message='{"attachment":{"type":"image", "payload":{}}}' \
;   -F filedata=@/tmp/red.jpg;type=image/jpg \
;   "https://graph.facebook.com/v2.6/me/messages?access_token=${PAGE_ACCESS_TOKEN}"

; curl -X GET "https://graph.facebook.com/v2.6/<USER_ID>?fields=first_name,last_name,profile_pic,locale,timezone,gender&access_token=PAGE_ACCESS_TOKEN"

;;.
