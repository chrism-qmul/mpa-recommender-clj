(ns mpa-recommender-clj.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [mpa-recommender.clj.db :as recommender]))

(defroutes app
  (GET "/task/:uuid" [uuid]
       (->> (load-db-data)
            (recommender/mpa)
            (recommender/item-scores-for-annotator uuid)
            (recommender/aggregate-doc-scores)
            (sort-by val >)
            (first)
            (key)))
  (POST "/task" request
       (recommender/log-annotation 1)))
