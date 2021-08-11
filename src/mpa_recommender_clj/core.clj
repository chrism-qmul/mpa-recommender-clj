(ns mpa-recommender-clj.core
  (:require [mpa-recommender-clj.recommender :as recommender]
            [mpa-recommender-clj.db :as db]
            [environ.core :refer [env]]
            [clojure.java.io :as io]
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
  (GET "/" req (slurp (io/resource "index.html")))
  (GET "/task/:uuid" [uuid]
       (response (recommender/best-recommendation-for-annotator uuid @latest-model)))
  (POST "/task" {:keys [body]}
        ;uuid itemid label
       (put! update-required-channel 1)
       (recommender/log-annotation body)
       (response {:successfull true}))
  (route/not-found "not found"))


(defn run-webserver [join?]
  (let [app-middleware (-> app
                           (wrap-json-body {:keywords? true})
                           (wrap-json-response))]
    (jetty/run-jetty app-middleware {:port (Integer/parseInt (env :http-port))
                                     :join? join?})))

(defn check-env []
  (and (some? (env :http-port)) (some? (env :database-path))))

(defn -main [& args]
  (if (check-env)
    (do
      (db/create-db)
      (model-updater)
      (run-webserver true))
    (prn "Missing config DATABASE_PATH or HTTP_PORT")))
