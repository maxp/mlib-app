
(ns mlib.mdb.util
  ; (:require
  ;   [mlib.log :refer [warn]]
  (:import
    [org.bson.types ObjectId]))
;

(defn id_id [r]
  (if-let [id (:_id r)]
    (assoc (dissoc r :_id) :id id)
    r))
;

(defn new_id []
  (ObjectId.))
;

(defn oid [s]
  (try 
    (ObjectId. s) 
    (catch Exception e 
      (str s))))
;

;;; ;;; ;;; ;;; ;;;

; (def seq-coll      "seq")
;
;
; (defn next-serial [seq-name]
;   (try
;     (long (:n (mc/find-and-modify (dbc) seq-coll
;                 {:_id (name seq-name)}
;                 {"$inc" {:n (int 1)}}
;                 {:return-new true :upsert true})))
;     (catch Exception e (warn "db/next-serial:" e))))
; ;
;
; (defn next-sn [sn]
;   (try
;     (str (:n (mc/find-and-modify (dbc) seq-coll
;                 {:_id (name sn)}
;                 {"$inc" {:n (int 1)}}
;                 {:return-new true :upsert true})))
;     (catch Exception e (warn "db/next-sn:" e))))

;;.
