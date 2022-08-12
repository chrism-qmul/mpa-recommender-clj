(ns mpa-recommender-clj.recommender
  (:require [clojure.string :refer [split]]
	[mpa-recommender-clj.db :as db]
	[mpa-recommender-clj.mpa :as mpa]
	[mpa-recommender-clj.information :as information]
	[mpa-recommender-clj.knowledge-tracing :as knowledge-tracing]
        [clojure.core.async :as async]
        [clojure.core.async :refer [thread go-loop <! dropping-buffer chan put!]]))

(defn build-mpa-model
  "build a new MPA model from the latest data"
  []
  (mpa/mpa (db/all)))

(defn log-annotation [annotation latest-model]
  (db/insert! annotation)
  (swap! latest-model assoc :outdated true))

(defn user-document-information [uuid mpa-model]
  (information/highest-information-for-annotator uuid mpa-model))


(defn user-reading-level [uuid mpa-model]
  (knowledge-tracing/predict-user-ability-from-cache uuid (mpa/get-silver mpa-model)))

(defn- best-reading-level-for-skill [uuid mpa-model]
  (let [default (zipmap (range 5) (repeat 0.0))
        reading-levels (user-reading-level uuid mpa-model)]
    (into {} (map (fn [[skill levels]]
           [skill (->> levels
               (merge default)
               (drop-while (fn [[k v]] (> v 0.75)))
               (first)
               (first))]) reading-levels))))

(defn best-document [uuid mpa-model]
  (let [user-ability (user-reading-level uuid mpa-model)
        user-reading-level (best-reading-level-for-skill uuid mpa-model)
        document-reading-levels (db/reading-level-for-items)
        max-reading-level-across-skills (first (max (vals user-reading-level)))
        document-information (user-document-information uuid mpa-model)
        item-has-reading-level? (fn [reading-level item] (= reading-level (get document-reading-levels item)))
        first-doc-at-reading-level (first (filter (partial item-has-reading-level? max-reading-level-across-skills) document-information))]
    first-doc-at-reading-level))

(defn model-updater [latest-model update-required-channel]
  (go-loop []
           (when-not (nil? @latest-model) (<! update-required-channel))
           (swap! latest-model assoc :mpa (<! (thread (build-mpa-model))))
           (recur)))
