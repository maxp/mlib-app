;;
;;  _PRJ_
;;

(def project {:name "_PRJ_" :version "0.0.0"})

(def jar-main '_PRJ_.main)
(def jar-file "_PRJ_.jar")


(set-env!
  :resource-paths #{"res"}
  :source-paths #{"src"}
  :asset-paths #{"res"}

  ;; boot -d boot-deps ancient
  :dependencies
  '[
    [org.clojure/clojure "1.8.0"]
    ; [org.clojure/core.cache "0.6.4"]

    [com.taoensso/timbre "4.8.0"]   ; https://github.com/ptaoussanis/timbre
    [org.clojure/tools.logging "0.3.1"]
    [ch.qos.logback/logback-classic "1.1.8"]

    [clj-time "0.13.0"]
    [clj-http "3.4.1"]

    ; [javax.servlet/servlet-api "2.5"]
    ; [http-kit "2.1.19"]
    [ring/ring-core "1.5.0"]
    [ring/ring-json "0.4.0"]
    [ring/ring-headers "0.2.0"]
    [ring/ring-jetty-adapter "1.5.0"]

    [cheshire "5.6.3"]
    [compojure "1.5.1"]
    [hiccup "1.0.5"]
    [mount "0.1.11"]

    [rum "0.10.7"]

    ;;
    [org.clojure/java.jdbc "0.6.1"]
    [org.postgresql/postgresql "9.4.1212"]
    ;  [com.mchange/c3p0 "0.9.5.2"]
    [honeysql "0.8.2"]    ; https://github.com/jkk/honeysql

    [com.novemberain/monger "3.1.0"]

    ; [org.postgresql/postgresql "9.4.1212"]

    ;; https://funcool.github.io/clojure.jdbc/latest/
    ; [funcool/clojure.jdbc "0.9.0"]
    ;; https://github.com/tomekw/hikari-cp
    ; [hikari-cp "1.7.5"]

    ; [com.draines/postal "2.0.2"]

    ;; https://github.com/martinklepsch/boot-garden
    ; [org.martinklepsch/boot-garden "1.3.2-0" :scope "test"]

    ;; [enlive "1.1.5"]     ;; https://github.com/cgrand/enlive

    ;; repl
    [org.clojure/tools.namespace "0.2.11" :scope "test"]
    [proto-repl "0.3.1" :scope "test"]])
;

(task-options!
  aot {:all true}
  ; garden {
  ;         :styles-var 'css.styles/main
  ;         :output-to  "public/incs/css/main.css"
  ;         :pretty-print false}
  repl {:init-ns 'user})

;;;;;;;;;


(require
  '[clojure.tools.namespace.repl :refer [set-refresh-dirs]]
  '[clojure.edn :as edn]
  '[clj-time.core :as tc]
  '[boot.git :refer [last-commit]])
  ; '[org.martinklepsch.boot-garden :refer [garden]]


;;;;;;;;;

(defn increment-build []
  (let [bf "res/build.edn"
        num (:num (edn/read-string (slurp bf)))
        bld { :timestamp (str (tc/now))
              :commit (last-commit)
              :num (inc num)}]
    (spit bf (.toString (merge project bld)))))
;

(deftask test-env []
  (set-env! :source-paths #(conj % "test"))
  identity)
;

(deftask dev []
  (set-env! :source-paths #(conj % "dev" "test"))
  (apply set-refresh-dirs (get-env :source-paths))
  identity)
;

; (deftask css-dev []
;   (comp
;     (watch)
;     (garden :pretty-print true)
;     (target :dir #{"tmp/res/"})))
; ;


(deftask build []
  (increment-build)
  (comp
    (javac)
    ; (garden)
    (aot)
    (uber)
    (jar :main jar-main :file jar-file)
    (target :dir #{"tmp/target"})))
;

;;.
