
(def project "myapp")
(def version "0.18.0")

(def main-class (symbol (str project ".main")))

(set-env!
  :source-paths   #{"src"}
  :resource-paths #{"resources"}
  :asset-paths    #{"resources"}

  ;; boot -d boot-deps ancient
  :dependencies
  '[
    [org.clojure/clojure "1.9.0"]
    [org.clojure/core.async "0.4.474"]
    ; [org.clojure/core.cache "0.6.4"]

    [org.clojure/tools.logging "0.4.0"]
    [ch.qos.logback/logback-classic "1.2.3"]

    [clj-time "0.14.2"]
    [clj-http "3.7.0"]

    ; [javax.servlet/servlet-api "2.5"]
    ; [http-kit "2.1.19"]
    [ring/ring-core "1.6.3"]
    [ring/ring-json "0.4.0"]
    [ring/ring-headers "0.3.0"]
    [ring/ring-jetty-adapter "1.6.3"]

    [cheshire "5.8.0"]
    [compojure "1.6.0"]
    [hiccup "1.0.5"]

    [mount "0.1.11"]

    [com.novemberain/monger "3.1.0"]

    [org.postgresql/postgresql "42.2.1"]

    ;; https://funcool.github.io/clojure.jdbc/latest/
    [funcool/clojure.jdbc "0.9.0"]
    [hikari-cp "2.0.1"]   ; https://github.com/tomekw/hikari-cp
    [honeysql "0.9.1"]    ; https://github.com/jkk/honeysql

    ;; [org.clojure/java.jdbc "0.6.1"]
    ;; [com.mchange/c3p0 "0.9.5.2"]

    ; [com.draines/postal "2.0.2"]

    ;; https://github.com/martinklepsch/boot-garden
    ; [org.martinklepsch/boot-garden "1.3.2-1" :scope "test"]

    ;; repl
    [org.clojure/tools.namespace "0.2.11" :scope "test"]
    [proto-repl "0.3.1" :scope "test"]])
;

(task-options!
  aot {:all true})
  ; garden {
  ;         :styles-var 'css.styles/main
  ;         :output-to  "public/incs/css/main.css"
  ;         :pretty-print false}
  ; repl {:init-ns 'user})

;;;;;;;;;


(require
  '[clojure.tools.namespace.repl :refer [set-refresh-dirs refresh]]
  '[clojure.edn :as edn]
  '[clj-time.core :as tc]
  '[mount.core :as mount]
  '[boot.git :refer [last-commit]])
  ; '[org.martinklepsch.boot-garden :refer [garden]]


;;;;;;;;;

(deftask test-env []
  (set-env! :source-paths #(conj % "test"))
  identity)
;

(deftask dev []
  (set-env! :source-paths #(conj % "test"))
  (apply set-refresh-dirs (get-env :source-paths))
  (javac))
;  identity)
;

; (deftask css-dev []
;   (comp
;     (watch)
;     (garden :pretty-print true)
;     (target :dir #{"tmp/resources/"})))
; ;

;;; ;;; ;;; ;;;

(defn start []
  (require main-class)
  (-> "tmp/dev.edn"
    (slurp)
    (edn/read-string)
    (mount/start-with-args)))
;

(defn stop []
  (mount/stop))

(defn go []
  (stop)
  (apply set-refresh-dirs (get-env :source-paths))
  (refresh :after 'boot.user/start))
;


(defn build-clj []
  (let [build-clj (str "src/" project "/build.clj")
        bld { :name project 
              :version version
              :timestamp (str (tc/now))
              :commit (last-commit)}]
    (spit build-clj 
      (str 
        "(ns " project ".build)\n" 
        "(def build " bld ")\n"))))
;

(deftask build []
  (build-clj)
  (comp
    (javac)
    ; (garden)
    (aot)
    (uber)
    (jar :main main-class :file (str project ".jar"))
    (target :dir #{"tmp/target"})))
;

;;.
