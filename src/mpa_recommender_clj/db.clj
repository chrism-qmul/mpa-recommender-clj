(ns mpa-recommender-clj.db
 (:require [clojure.java.jdbc :as jdbc]
           [clojure.java.io :as io]
           [clojure.spec.alpha :as s]
           [spec-tools.core :as st]
           [environ.core :refer [env]])
 (:gen-class))

(s/def ::uuid string?)
(s/def ::reading_level int?)
(s/def ::itemid (s/and string? #(re-matches #".*;.*" %)))
(s/def ::label (s/and string? #(re-matches #".*?\(.*?\)" %)))

(s/def ::annotation (s/keys :req-un [::uuid ::itemid ::reading_level ::label]))

(defn coerce-annotation [annotation]
  (st/coerce ::annotation annotation  st/string-transformer))

(defn valid-annotation? [annotation]
  (s/valid? ::annotation annotation))

(defn valid-annotations? [annotations]
  (s/valid? (s/coll-of ::annotation) annotations))

(def dbpath (env :database-path))

(def db
  {:connection-uri (str "jdbc:sqlite:" dbpath)})

(defn file-exists? 
  "check if file at path exists"
  [path]
  (.exists (io/as-file path)))

(defn create-db
 "create db and table"
 []
 (when-not (file-exists? dbpath)
 (try 
  (jdbc/db-do-commands db
   [(jdbc/create-table-ddl :annotations
    [[:uuid :text]
    [:collection :text]
    [:itemid :text]
    [:reading_level :int]
    [:timestamp :datetime :default :current_timestamp]
    [:label :text]])
   "CREATE INDEX annotations_uuid ON annotations(uuid)"
   "CREATE INDEX annotations_collection ON annotations(collection)"
   "CREATE INDEX annotations_itemid ON annotations(itemid)"])
  (catch Exception e
   (println (.getMessage e))))))

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

(defn items-for-collection
  "all the items in a collection"
  [collection]
  (map :itemid (jdbc/query db ["select itemid from annotations where collection = ?" collection])))

(defn all
  "get all annotation records"
  []
  (jdbc/query db ["select * from annotations"]))

(defn with-reading-level
  "get all annotation records with reading level"
  []
  (jdbc/query db ["select * from annotations where reading_level > 0"]))

(defn top-annotators-at-level
  "get top contributing annotators at level"
  [reading_level limit]
  (jdbc/query db ["select * from (select count() as annotation_count, uuid from annotations where reading_level = ? GROUP BY uuid) order by annotation_count desc LIMIT ?" reading_level limit]))

(defn seen-items-for-annotator
  "seen items for annotator"
  [uuid]
  (map :itemid (jdbc/query db ["select itemid from annotations where uuid = ?" uuid])))

(defn annotations-for-user
  "seen items for annotator"
  [uuid]
  (jdbc/query db ["select * from annotations where uuid = ?" uuid]))

(defn annotations-for-user-with-reading-level
  "seen items for annotator"
  [uuid]
  (jdbc/query db ["select * from annotations where uuid = ? AND reading_level > 0" uuid]))

(defn reading-level-for-items []
  (let [sql "select substr(itemid,0,65) as document, reading_level from annotations group by document"
        results (jdbc/query db [sql])]
    (reduce (fn [acc {:keys [document reading_level]}] 
              (assoc acc document reading_level)) {} results)))

(defn annotators []
  (map :uuid (jdbc/query db ["select DISTINCT(uuid) as uuid from annotations"])))
(defn annotators []
  (map :uuid (jdbc/query db ["select DISTINCT(uuid) as uuid from annotations"])))

(defn -main []
 (create-db))
