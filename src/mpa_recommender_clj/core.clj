(ns mpa-recommender-clj.core
  (:require [mpa-recommender-clj.recommender :as recommender]
            [environ.core :refer [env]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [response]]
            [clojure.core.async :refer [thread go-loop <! dropping-buffer chan put!]])
  (:gen-class))

(def update-required-channel (chan (dropping-buffer 1)))

(def latest-model (atom nil))

(defn build-mpa-model
  "build a new MPA model from the latest data"
  []
  (recommender/mpa (recommender/load-db-data)))

(defn model-updater []
  (go-loop []
           (when-not (nil? @latest-model) (<! update-required-channel))
           (reset! latest-model (<! (thread (build-mpa-model))))
           (recur)))

(defroutes app
  (GET "/" req "task recommender")
  (GET "/task/:uuid" [uuid]
       (response (recommender/best-recommendation-for-annotator uuid @latest-model)))
  (POST "/task" {:keys [body]}
        ;uuid itemid label
       (put! update-required-channel 1)
       (recommender/log-annotation body)
       (response {:successfull true})))


(defn run-webserver [join?]
  (let [app-middleware (-> app
                           (wrap-json-body {:keywords? true})
                           (wrap-json-response))]
    (jetty/run-jetty app-middleware {:port (Integer/parseInt (env :http-port))
                                     :join? join?})))


(defn -main []
  (model-updater)
  (run-webserver true))
