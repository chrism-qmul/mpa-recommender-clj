(ns mpa-recommender-clj.db
 (:require [clojure.java.jdbc :as jdbc]
           [clojure.java.io :as io]
           [clojure.spec.alpha :as s]
           [environ.core :refer [env]])
 (:gen-class))

(s/def ::uuid string?)
(s/def ::itemid (s/and string? #(re-matches #".*;.*" %)))
(s/def ::label (s/and string? #(re-matches #".*?\(.*?\)" %)))

(s/def ::annotation (s/keys :req-un [::uuid ::itemid ::label]))

(defn valid-annotation? [annotation]
  (s/valid? ::annotation annotation))

(defn valid-annotations? [annotations]
  (s/valid? (s/coll-of ::annotation) annotations))

(def dbpath (env :database-path))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     dbpath})

(defn file-exists? 
  "check if file at path exists"
  [path]
  (.exists (io/as-file path)))

(defn create-db
 "create db and table"
 []
 (try 
  (jdbc/db-do-commands db
   (jdbc/create-table-ddl :annotations
    [[:uuid :text]
    [:itemid :text]
    [:timestamp :datetime :default :current_timestamp]
    [:label :text]]))
  (catch Exception e
   (println (.getMessage e)))))

(defn mass-insert! 
  "fast insert multiple annotation records to the database"
  [data]
  {:pre [(valid-annotations? data)]}
 (jdbc/insert-multi! db :annotations data)
	nil)

(defn insert! 
  "insert a single annotation record to the database"
  [record]
  {:pre [(valid-annotation? record)]}
  (jdbc/insert! db :annotations record))

(defn all
  "get all annotation records"
  []
  (jdbc/query db ["select * from annotations"]))

(defn seen-items-for-annotator
  "seen items for annotator"
  [uuid]
  (map :itemid (jdbc/query db ["select itemid from annotations where uuid = ?" uuid])))

(defn annotators []
  (map :uuid (jdbc/query db ["select DISTINCT(uuid) as uuid from annotations"])))

(defn -main []
 (create-db))

; if the database doesn't exist on startup, create it
(when-not (file-exists? dbpath) (create-db))
