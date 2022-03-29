(ns mpa-recommender-clj.information
  (:require [clojure.string :refer [split]]
	[mpa-recommender-clj.db :as db]
	[mpa-recommender-clj.mpa :as mpa]
        [clojure.core.async :as async]))

(def latest-model (atom {:model nil :outdated true}))

(defn itemname->docid [itemname]
  (-> itemname
    (split #";" 2)
    (first)))

(defn aggregate-doc-scores [item-scores & {:keys [aggregate] :or {aggregate max}}]
 (reduce-kv (fn [m itemid score] 
	     (let [docid (itemname->docid itemid)]
	      (update m docid (fnil aggregate 0) score))) {} item-scores))

(defn seen-documents-for-annotator [uuid]
  (->> uuid
    (db/seen-items-for-annotator)
    (map itemname->docid)
    (set)))

;(defn highest-agreement [model]
;  (->> model

(defn highest-information-for-annotator [uuid model]
  (->> model
       (mpa/item-scores-for-annotator uuid)
       (aggregate-doc-scores)
       (sort-by val >)
       (map key)
       (remove (seen-documents-for-annotator uuid))
       (first)))

;(seen-documents-for-annotator (first (db/annotators)))
;(def model (mpa (load-db-data)))
(comment (highest-information-for-annotator (first (db/annotators)) (mpa/mpa (db/all))))

;(defn updater 
;  "keeps an up to date model as changes come in"
;  []
;  (let [latest-model (async/chan (async/sliding-buffer 1))
;        update-channel (async/chan (async/dropping-buffer 1))
;        output-channel (async/chan 1)]
;    (async/thread
;      (async/go-loop []
;               (when-not (nil? @latest-model) (async/<! update-channel))
;               (async/>! latest-model (mpa (load-db-data)))
;               (recur)))
;    (async/go-loop []
;                   (async/>! update-channel @latest-model)
;                   (recur))
;    model-channel))
