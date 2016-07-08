;;
;;  _PRJ_
;;

(def project
  { :name    "_PRJ_" 
    :version "0.0.0"})

(def jar-main '_PRJ_.main)
(def jar-file "_PRJ_.jar")
(def dev-main '_PRJ_.srv)


(set-env!
  :resource-paths #{"res"}
  :source-paths #{"src"}
  :asset-paths #{"res"}

  ;; boot -d boot-deps ancient

  :dependencies
  '[
    [org.clojure/clojure "1.8.0"]
    [org.clojure/tools.namespace "0.2.11" :scope "test"]
    [com.taoensso/timbre "4.4.0"]   ; https://github.com/ptaoussanis/timbre
    ; [org.clojure/core.cache "0.6.4"]

    [clj-time "0.12.0"]
    [clj-http "3.1.0"]

    ; [javax.servlet/servlet-api "2.5"]
    ; [http-kit "2.1.19"]
    [ring/ring-core "1.5.0"]
    [ring/ring-json "0.4.0"]
    [ring/ring-headers "0.2.0"]
    [ring/ring-jetty-adapter "1.5.0"]

    [cheshire "5.6.2"]
    [compojure "1.5.1"]
    [hiccup "1.0.5"]
    [mount "0.1.10"]

    [org.clojure/java.jdbc "0.5.8"]
    [org.postgresql/postgresql "9.4.1208"]
    [com.mchange/c3p0 "0.9.5.2"]
    [honeysql "0.6.3"]])  ; https://github.com/jkk/honeysql
;

(task-options!
  aot {:all true})

;;;;;;;;;


(require
  '[clojure.tools.namespace.repl :as repl]
  '[clojure.edn :as edn]
  '[clj-time.core :as tc]
  '[boot.git :refer [last-commit]]
  '[mount.core :as mount])

(require dev-main)


;;;;;;;;;


(defn start []
  (let [rc (edn/read-string (slurp "var/dev.edn"))]
    (mount/start-with-args rc)))
;

(defn go []
  (mount/stop)
  (apply repl/set-refresh-dirs (get-env :source-paths))
  (repl/refresh :after 'boot.user/start))
;

(defn increment-build []
  (let [bf "res/build.edn"
        num (:num (edn/read-string (slurp bf)))
        bld { :timestamp (str (tc/now))
              :commit (last-commit)
              :num (inc num)}]
    (spit bf (.toString (merge project bld)))))
;

(deftask build []
  (increment-build)
  (comp
    (aot)
    (uber)
    (jar :main jar-main :file jar-file)
    (target :dir #{"tmp/target"})))
;

;;.
