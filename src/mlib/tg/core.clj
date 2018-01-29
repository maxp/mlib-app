
(ns mlib.tg.core
  (:require
    [mlib.log :refer [info warn]]
    [clj-http.client :as http]
    [cheshire.core :refer [parse-string]]))
;


(def TG_RETRY 3)
(def TG_TIMEOUT 30000)
(def TG_PAUSE 20)

(def E_RETRY  ::error-retry)
(def E_SOCKET ::error-socket)
(def E_TGAPI  ::error-tgapi)


(defn api-url [token method]
  (str "https://api.telegram.org/bot" token "/" (name method)))
;

(defn file-url [token path]
  (str "https://api.telegram.org/file/bot" token "/" path))
;

(defn api [token method params & [{timeout :timeout retry :retry}]]
  (Thread/sleep TG_PAUSE)
  (let [tout  (or timeout TG_TIMEOUT)
        retry (or retry   TG_RETRY)]
      (try
        (let [{status :status  body :body}
              (http/post (api-url token method)
                  { :content-type :json
                    :as :json
                    :form-params params
                    :throw-exceptions false
                    :socket-timeout tout
                    :conn-timeout tout})]
          (cond
            (= status 200)
            (if (:ok body)
              (:result body)
              (do
                (warn "tg-api:" method body)
                (assoc body :error E_TGAPI)))
            ;
            (= status 303)
            (if (< 0 retry)
              (api token method params {:timeout timeout :retry (dec retry)})
              (do
                (warn "tg-api:" method "retry limit reached")
                {:error E_RETRY}))
            ;
            :else
            (do
              (warn "tg-api:" method body)
              (assoc (parse-string body true) :error E_TGAPI))))
          ;
        (catch Exception e
          (do
            (warn "tg-api:" method (.getMessage e))
            {:error E_SOCKET})))))
      ;;
;

(defn send-text [token chat text & [parse-mode]]
  (api token :sendMessage
    (merge {:chat_id chat :text text}
      (when parse-mode [:parse_mode parse-mode]))))
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
            :socket-timeout (or timeout TG_TIMEOUT)
            :conn-timeout   (or timeout TG_TIMEOUT)}))
      (catch Exception e
        (warn "get-file:" file-id (.getMessage e))))
    ;
    (info "get-file - not path for file_id:" file-id)))
;

(defn send-file
  "params should be stringable (json/generate-string)
    or File/InputStream/byte-array"
  [token method mpart & [{timeout :timeout}]]
  (try
    (let [tout (or timeout TG_TIMEOUT)
          res (:body
                (http/post (api-url token method)
                  { :multipart
                      (for [[k v] mpart]
                        {:name (name k) :content v :encoding "utf-8"})
                    :as :json
                    :throw-exceptions false
                    :socket-timeout tout
                    :conn-timeout tout}))]
          ;
      (if (:ok res)
        (:result res)
        (info "send-file:" method res)))
    (catch Exception e
      (warn "send-file:" method (.getMessage e)))))
;


(defn set-webhook-cert [token url cert-file]
  (http/post (api-url token :setWebhook)
    {:multipart [ {:name "url" :content url}
                  {:name "certificate" :content cert-file}]}))
;

;;.
