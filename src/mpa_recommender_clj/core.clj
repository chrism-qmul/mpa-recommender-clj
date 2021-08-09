(ns mpa-recommender-clj.core
  (:require [mpa-recommender-clj.recommender :as recommender]
            [environ.core :refer [env]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [clojure.core.async :as async])
  (:gen-class))

;(defroutes app
;  (GET "/task/:uuid" [uuid]
;       )
;  (POST "/task" request
;       (comment (recommender/log-annotation 1))
;       ""))

(defn build-mpa-model
  "build a new MPA model from the latest data"
  []
  (recommender/mpa (recommender/load-db-data)))

(thread (build-mpa-model))

(defn run-webserver []
  (jetty/run-jetty app {:port (env :http-port)
                            :join? false}))

(defn -main []
  0)
