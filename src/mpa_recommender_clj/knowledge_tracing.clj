(ns mpa-recommender-clj.knowledge-tracing
  (:require [bkt.core :as bkt]
            [mpa-recommender-clj.mpa :as mpa]
            [medley.core :refer [deep-merge]]
            [com.rpl.specter :refer [transform select ALL MAP-VALS]]
            [mpa-recommender-clj.db :as db]))

(defn get-mpa-model []
  (->> (db/all)
       (mpa/mpa)))

(def silver (comp mpa/get-silver get-mpa-model))

(def label->skill #(re-find #"[^(]*" %)))

(defn is-silver? [silver-annotations {:keys [itemid label]}]
  (= label (get silver-annotations itemid)))

(defn annotations->bkt-data [correct? user-annotations]
  (let [annotation-skill (fn [{:keys [label]}] (label->skill label))]
    (->> user-annotations
         (group-by annotation-skill)
         (transform [MAP-VALS] #(group-by :reading_level %))
         (transform [MAP-VALS MAP-VALS] #(vec (vals (group-by :uuid %))))
         (transform [MAP-VALS MAP-VALS ALL ALL] correct?)
         )))

(defn fit-bkt-models [user-annotations silver-annotations]
  (let [correct? #(is-silver? silver-annotations %)
        fit-bkt-model (fn [annotations] {:model (future (bkt/fit-model annotations bkt/guess-slip-bounds))})]
    (->> user-annotations
         (annotations->bkt-data correct?)
         (transform [MAP-VALS MAP-VALS] fit-bkt-model)
         )))

(defn predict-user-ability [user model-parameters silver-annotations]
  (let [user-annotations (db/annotations-for-user-with-reading-level user)
        correct? #(is-silver? silver-annotations %)]
    (->> user-annotations
         (annotations->bkt-data correct?)
        (transform [MAP-VALS MAP-VALS] (fn [xs] {:annotations (first xs)}))
        (deep-merge model-parameters)
        (transform [MAP-VALS MAP-VALS] (fn [{:keys [annotations model]}] (last (bkt/predict-known annotations (@model :params)))))
    )))

(comment (let [model-parameters (fit-bkt-models (take 1000 (db/with-reading-level)) (silver))]
(predict-user-ability "chris" model-parameters (silver))))
