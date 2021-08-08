(ns mpa-recommender-clj.recommender
  (:import (MPA Model))
  (:require [clojure.string :refer [split]]
	[mpa-recommender-clj.db :as db]))

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

;(aggregate-doc-scores (item-scores-for-annotator "5395fc8b1cc10b8840f7502f0798a1420bf928a7de93931b02024e0064811d57" (mpa (load-db-data))))

(defn item-scores-for-annotator [annotator mpa-model]
 (let [item-names (get-item-names mpa-model)
  inf (seq (.calculateMutualInformationForAnnotator mpa-model annotator))]
  (zipmap item-names inf)))

(defn aggregate-doc-scores [item-scores & {:keys [aggregate] :or {aggregate max}}]
 (reduce-kv (fn [m itemid score] 
	     (let [docid (itemname->docid itemid)]
	      (update m docid (fnil aggregate 0) score))) {} item-scores))
