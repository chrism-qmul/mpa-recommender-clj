(ns mpa-recommender-clj.core
  (:import (MPA Model))
  (:require [clojure.string :refer [split]]))

(defrecord Annotation [annotator item_id context label])

(defn annotation->mpa [annotation]
  (into-array [(.item_id annotation)
               (.annotator annotation)
               (str (.context annotation) "(" (.label annotation) ")")]))

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

(let [mpa-model (mpa [(Annotation. "bob" "xyz" "T" "1;2") (Annotation. "alice" "xyz" "T" "1;2")])]
  (seq (.calculateMutualInformationForAnnotator mpa-model "bob")))

(def sample-annotations [["doc_a;item_a" "alice" "T" "1;1"]
                  ["doc_a;item_a" "bob" "T" "1;2"]
                  ["doc_a;item_a" "charlie" "T" "1;1"]
                  ["doc_a;item_a" "dave" "T" "2;2"]
                  ["doc_a;item_b" "alice" "T" "1;1"]
                  ["doc_a;item_b" "bob" "T" "1;1"]
                  ["doc_a;item_b" "dave" "T" "2;2"]
                  ["doc_b;item_c" "alice" "F" "2;2"]
                  ["doc_b;item_c" "bob" "F" "1;1"]
                  ["doc_b;item_c" "charlie" "F" "1;1"]
                  ["doc_b;item_c" "dave" "T" "1;1"]
                  ["doc_b;item_d" "alice" "F" "1;1"]
                  ["doc_b;item_e" "bob" "F" "1;1"]])

(def sample (mapv (fn [[item annotator context label]] (Annotation. annotator item context label)) sample-annotations))

(seq (.calculateMutualInformationForAnnotator (mpa sample) "bob"))

(.getRawItem (mpa sample) 1)

(defn itemname->docid [itemname]
  (-> itemname
    (split #";" 2)
    (first)))

(into {} [:a :b :c] [4 5 6])

(let [mpa-model (mpa sample)
      item-names (get-item-names mpa-model)
      inf (seq (.calculateMutualInformationForAnnotator mpa-model "alice"))]
  (->> inf
    (group-by (fn [[item inf]] (itemname->docid item)) (zipmap item-names))))
  ;(group-by (fn [[item inf]] (itemname->docid item)) (map vector item-names inf)))

;(reduce-kv (fn [init k v] ()) {} {"xyz;1" 1 "xyz;2" 2 "abc;1" 3 "abc;2" 4})

(get-item-names (mpa sample))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
