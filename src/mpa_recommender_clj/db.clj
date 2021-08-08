(ns mpa-recommender-clj.db
 (:require [clojure.java.jdbc :refer :all])
 (:gen-class))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"
   })

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

(take 5 (all))

(defn -main []
 (create-db))
