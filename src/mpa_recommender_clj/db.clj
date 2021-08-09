(ns mpa-recommender-clj.db
 (:require [clojure.java.jdbc :refer :all]
           [clojure.java.io :as io])
 (:gen-class))

(def dbpath "db/database.db")

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     dbpath
   })

(defn file-exists? [path]
  (.exists (io/as-file path)))

(defn create-db
 "create db and table"
 []
 (try 
  (db-do-commands db
   (create-table-ddl :annotations
    [[:uuid :text]
    [:itemid :text]
    [:timestamp :datetime :default :current_timestamp]
    [:label :text]]))
  (catch Exception e
   (println (.getMessage e)))))

(defn mass-insert [data]
 (insert-multi! db :annotations data)
	nil)

(defn all
  "get all"
  []
  (query db ["select * from annotations"]))

(defn -main []
 (create-db))

(when-not (file-exists? dbpath) (create-db))
