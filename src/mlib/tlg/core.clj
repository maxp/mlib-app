
(ns mlib.tlg.core
  (:require
    [mlib.log :refer [info warn]]
    [clj-http.client :as http]))
;


(def socket-timeout 5000)

(defn api-url [token method]
  (str "https://api.telegram.org/bot" token "/" (name method)))
;

(defn file-url [token path]
  (str "https://api.telegram.org/file/bot" token "/" path))
;

(defn api
  [token method params & [{timeout :timeout}]]
  (try
    (let [tout (or timeout socket-timeout)
          res (:body
                (http/post (api-url token method)
                  { :content-type :json
                    :as :json
                    :form-params params
                    :socket-timeout tout
                    :conn-timeout tout}))]
      (if (:ok res)
        (:result res)
        (info "api-fail:" method res)))
    (catch Exception e
      (warn "tg-api:" method e))))
;

(defn send-text [token chat text & [markdown?]]
  (api token :sendMessage
    (merge {:chat_id chat :text text}
      (when markdown? [:parse_mode "Markdown"]))))
;

(defn send-md [token chat text]
  (api token :sendMessage
    {:chat_id chat :text text :parse_mode "Markdown"}))
;

(defn send-html [token chat text]
  (api token :sendMessage
    {:chat_id chat :text text :parse_mode "HTML"}))
;

(defn send-message [token chat params]
  (api token :sendMessage (merge {:chat_id chat} params)))
;

(defn file-path [token file-id]
  ;; {:file_id "..." :file_size 999 :file_path "dir/file.ext"}
  (:file_path
    (api token :getFile {:file_id file-id})))
;

(defn get-file [token file-id & [{timeout :timeout}]]
  (if-let [path (file-path token file-id)]
    (try
      (:body
        (http/get (file-url token path)
          { :as :byte-array
            :socket-timeout (or timeout socket-timeout)
            :conn-timeout   (or timeout socket-timeout)}))
      (catch Exception e
        (warn "get-file:" file-id e)))
    ;
    (info "get-file - not path for file_id:" file-id)))
;

(defn send-file
  "params should be stringable (json/generate-string)
    or File/InputStream/byte-array"
  [token method mpart & [{timeout :timeout}]]
  (try
    (let [tout (or timeout socket-timeout)
          res (:body
                (http/post (api-url token method)
                  { :multipart
                      (for [[k v] mpart]
                        {:name (name k) :content v :encoding "utf-8"})
                    :as :json
                    :socket-timeout tout
                    :conn-timeout tout}))]
          ;
      (if (:ok res)
        (:result res)
        (info "send-file:" method res)))
    (catch Exception e
      (warn "send-file:" method e))))
;


(defn set-webhook-cert [token url cert-file]
  (http/post (api-url token :setWebhook)
    {:multipart [ {:name "url" :content url}
                  {:name "certificate" :content cert-file}]}))
;

;;.
