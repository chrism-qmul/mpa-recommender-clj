(ns mpa-recommender-clj.recommender
  (:import (MPA Model))
  (:require [clojure.string :refer [split]]
	[mpa-recommender-clj.db :as db]
        [clojure.core.async :as async]))


(def latest-model (atom {:model nil :outdated true}))

(defrecord Annotation [annotator item_id label]) ;context(label)

(defn annotation->mpa [annotation]
  (into-array [(.item_id annotation)
               (.annotator annotation)
               (.label annotation)]))

(defn mpa [annotationsin]
  (let [annotations (->> annotationsin
                         (map annotation->mpa)
                         (into-array))
        mpa-model (Model.)]
    (doto mpa-model
      (.LoadAnnotations annotations)
      (.ProcessAnnotations)
      (.InitializeParameterSpace)
      (.InitializeParameters)
      (.RunModel)
      (.ComputePosteriorEstimates))
    mpa-model))

(defn get-item-names [mpa]
  (let [I (.getI mpa)]
    (mapv #(.getRawItem mpa %) (range I))))

(defn itemname->docid [itemname]
  (-> itemname
    (split #";" 2)
    (first)))

(defn load-db-data []
 (map (fn [{annotator :uuid itemid :itemid label :label}]
	(->Annotation annotator itemid label)) (db/all)))

(defn log-annotation [annotation]
  (db/insert! annotation)
  (swap! latest-model assoc :outdated true))

(defn item-scores-for-annotator [annotator mpa-model]
 (let [item-names (get-item-names mpa-model)
  inf (seq (.calculateMutualInformationForAnnotator mpa-model annotator))]
  (zipmap item-names inf)))


(defn aggregate-doc-scores [item-scores & {:keys [aggregate] :or {aggregate max}}]
 (reduce-kv (fn [m itemid score] 
	     (let [docid (itemname->docid itemid)]
	      (update m docid (fnil aggregate 0) score))) {} item-scores))

(defn seen-documents-for-annotator [uuid]
  (->> uuid
    (db/seen-items-for-annotator)
    (map itemname->docid)
    (set)))

(defn best-recommendation-for-annotator [uuid model]
  (->> model
       (item-scores-for-annotator uuid)
       (aggregate-doc-scores)
       (sort-by val >)
       (map key)
       (remove (seen-documents-for-annotator uuid))
       (first)))

;(seen-documents-for-annotator (first (db/annotators)))
;(def model (mpa (load-db-data)))
;(best-recommendation-for-user (first (db/annotators)) model)

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
