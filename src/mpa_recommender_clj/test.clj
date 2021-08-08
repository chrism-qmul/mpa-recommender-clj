(ns mpa-recommender-clj.test
  (:require [mpa-recommender-clj.recommender :as recommender]))

(def sample-annotations [
		  ["doc_a" "item_a" "alice" "T(1;1)"]
                  ["doc_a" "item_a" "bob" "T(1;2)"]
                  ["doc_a" "item_a" "charlie" "T(1;1)"]
                  ["doc_a" "item_a" "dave" "T(2;2)"]
                  ["doc_a" "item_b" "alice" "T(1;1)"]
                  ["doc_a" "item_b" "bob" "T(1;1)"]
                  ["doc_a" "item_b" "dave" "T(2;2)"]
                  ["doc_b" "item_c" "alice" "F(2;2)"]
                  ["doc_b" "item_c" "bob" "F(1;1)"]
                  ["doc_b" "item_c" "charlie" "F(1;1)"]
                  ["doc_b" "item_c" "dave" "T(1;1)"]
                  ["doc_b" "item_d" "alice" "F(1;1)"]
                  ["doc_b" "item_e" "bob" "F(1;1)"]])

(def sample (mapv (fn [[doc_id item annotator label]] (recommender/->Annotation annotator doc_id item label)) sample-annotations))

(->> sample
(recommender/mpa)
(recommender/item-scores-for-annotator "bob")
(recommender/aggregate-doc-scores)
(sort-by val >)
(first)
(key))
