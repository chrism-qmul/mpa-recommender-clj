(ns mpa-recommender-clj.mpa
  (:import (MPA Model)))


(defn annotation->mpa [{:keys [itemid uuid label]}]
  (into-array [itemid uuid label]))

(defn get-silver [mpa-model]
  (->> mpa-model
    (.getResults)
    (into {})))

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

(defn item-scores-for-annotator [annotator mpa-model]
 (let [item-names (get-item-names mpa-model)
  inf (seq (.calculateMutualInformationForAnnotator mpa-model annotator))]
  (zipmap item-names inf)))

