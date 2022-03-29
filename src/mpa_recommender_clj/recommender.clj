(ns mpa-recommender-clj.recommender
  (:require [clojure.string :refer [split]]
	[mpa-recommender-clj.db :as db]
	[mpa-recommender-clj.mpa :as mpa]
        [clojure.core.async :as async]
        [clojure.core.async :refer [thread go-loop <! dropping-buffer chan put!]]))

(defn build-mpa-model
  "build a new MPA model from the latest data"
  []
  (mpa/mpa (db/all)))

(defn log-annotation [annotation latest-model]
  (db/insert! annotation)
  (swap! latest-model assoc :outdated true))

(defn model-updater [latest-model update-required-channel]
  (go-loop []
           (when-not (nil? @latest-model) (<! update-required-channel))
           (swap! latest-model assoc :mpa (<! (thread (build-mpa-model))))
           (recur)))
