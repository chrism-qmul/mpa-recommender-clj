(ns mpa-recommender-clj.import
 (:require [clojure.java.io :as io]
  [clojure.data.csv :as csv]
  [mpa-recommender-clj.recommender :as recommender]
  [mpa-recommender-clj.db :as db])
 (:gen-class))

(defn import-csv [csv-file-path]
 (with-open [reader (io/reader csv-file-path)]
  (doall (->> (csv/read-csv reader)
   (rest)
   (map (fn [[_ mention_id doc_id _ annotator_id _ label]]
	{:uuid annotator_id :itemid (str doc_id ";" mention_id) :label label}))))))

(defn csv-data->map [csv-data]
  (map zipmap (->> (first csv-data)
                   (map keyword)
                   repeat)
       (rest csv-data)))

(defn import-csv-basic [csv-file-path]
  (csv-data->map (-> csv-file-path (io/reader) (csv/read-csv))))

;(db/mass-insert! (import-csv-basic "ta_mpa.csv"))
;(take 5 )
;(db/mass-insert! (import-csv "ta.csv"))

;(defn -main []
; 1)
