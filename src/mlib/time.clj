
(ns mlib.time
  (:require
    [clj-time.core :as t]
    [clj-time.format :as tf]))
;    [clj-time.local :as lt]))
;

(defn now-ms [] (System/currentTimeMillis))
(defn now-date [] (java.util.Date.))

; (def tf-dmy (tf/formatter (t/default-time-zone) "dd.MM.yy" "dd.MM.yyyy" "yyyy-MM-dd"))

; (def tf-dmy (tf/formatter "dd.MM.yy" "dd.MM.yyyy" "yyyy-MM-dd"))

(def tf-hhmm     (tf/formatter "HH:mm" (t/default-time-zone)))
(def tf-hhmmss   (tf/formatter "HH:mm:ss" (t/default-time-zone)))

(def tf-ddmmyy   (tf/formatter "dd.MM.yy" (t/default-time-zone)))
(def tf-ddmmyyyy (tf/formatter "dd.MM.yyyy" (t/default-time-zone)))
(def tf-yyyymmdd (tf/formatter "yyyy-MM-dd" (t/default-time-zone)))

(def tf-ddmmyy-hhmm (tf/formatter "dd.MM.yy HH:mm" (t/default-time-zone)))
(def tf-ddmmyy-hhmmss (tf/formatter "dd.MM.yy HH:mm:ss" (t/default-time-zone)))

(def tf-yymmdd-hhmmss (tf/formatter "yyMMdd-HHmmss" (t/default-time-zone)))
(def tf-yyyymmdd-hhmmss (tf/formatter "yyyyMMdd-HHmmss" (t/default-time-zone)))
(def tf-iso-datetime (tf/formatter "yyyy-MM-dd HH:mm:ss" (t/default-time-zone)))

(defn parse-yyyymmdd [s]
  (try (tf/parse tf-yyyymmdd (str s)) (catch Exception ignore)))
(defn parse-ddmmyyyy [s]
  (try (tf/parse tf-ddmmyyyy (str s)) (catch Exception ignore)))
(defn parse-ddmmyy [s]
  (try (tf/parse tf-ddmmyy (str s)) (catch Exception ignore)))

(defn hhmm [date]
  (if date (try (tf/unparse tf-hhmm date) (catch Exception ignore))))
(defn hhmmss [date]
  (if date (try (tf/unparse tf-hhmmss date) (catch Exception ignore))))

(defn ddmmyy [date]
  (if date (try (tf/unparse tf-ddmmyy date) (catch Exception ignore))))
(defn ddmmyyyy [date]
  (if date (try (tf/unparse tf-ddmmyyyy date) (catch Exception ignore))))

(defn ddmmyy-hhmm [date]
  (if date (try (tf/unparse tf-ddmmyy-hhmm date) (catch Exception ignore))))
(defn ddmmyy-hhmmss [date]
  (if date (try (tf/unparse tf-ddmmyy-hhmmss date) (catch Exception ignore))))

(defn iso-date [date]
  (if date (try (tf/unparse tf-yyyymmdd date) (catch Exception ignore))))
(defn iso-datetime [ts]
  (if ts (try (tf/unparse tf-iso-datetime ts) (catch Exception ignore))))

;;.
