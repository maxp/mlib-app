
(ns mlib.web.middleware
  (:require
    [clojure.string :refer [starts-with?]]
    [ring.util.response :refer [get-header set-cookie]]
    [ring.middleware.json :refer [wrap-json-params]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.cookies :refer [wrap-cookies]]
    ;[ring.middleware.resource :refer [wrap-resource]]
    ;[ring.middleware.file :refer [wrap-file]]
    [ring.middleware.not-modified :refer [wrap-not-modified]]
    [ring.middleware.content-type :refer [wrap-content-type]]
    ;'[ring.middleware.proxy-headers :refer [wrap-forwarded-remote-addr]]
    [ring.middleware.absolute-redirects :refer [wrap-absolute-redirects]]
    [ring.middleware.x-headers :refer
        [wrap-xss-protection wrap-content-type-options wrap-frame-options]]))

    ;; NOTE: ring-cors
;

(defn wrap-csrf
  [handler & [{ignore-prefixes :ignore-prefixes}]]
  (fn [req]
    (let [{:keys [method uri sess headers]} req
          sess_csrf (:_csrf sess)]
      (if (and
            sess_csrf
            (= method :post)
            (not (some #(starts-with? uri %) ignore-prefixes))
            (not= sess_csrf (:x-csrf-token headers)))
        {:status 403 :headers {} :body "CSRF token mismatch"}
        ;;
        (let [resp (handler req)
              ctype (get-header resp "content-type")]
          (if (and sess_csrf (or (not ctype) (.startsWith ctype "text/html")))
            (set-cookie resp "_csrf"
              {:value sess_csrf :path "/" :http-only false})
            resp))))))
;

(defn middleware [handler]
  (-> handler
    (wrap-keyword-params)
    (wrap-json-params)
    (wrap-multipart-params)
    (wrap-params)
    (wrap-cookies)
    (wrap-absolute-redirects)
    (wrap-content-type)
    (wrap-not-modified)
    ; (wrap-frame-options (or :deny {:allow-from "url"}))
    (wrap-xss-protection true {:mode :block})   ; X-XSS-Protection: 1; mode=block
    (wrap-content-type-options :nosniff)))        ; X-Content-Type-Options: nosniff

    ; ssl
    ;        (wrap-hsts)
    ;        (wrap-ssl-redirect)
    ;        (wrap-forwarded-scheme)

;;.
