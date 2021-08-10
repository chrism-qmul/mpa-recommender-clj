(defproject mpa-recommender-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
  :url "https://www.eclipse.org/legal/epl-2.0/"}
  :main mpa-recommender-clj.core
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "1.3.618"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.xerial/sqlite-jdbc "3.36.0.1"]
                 [org.clojure/data.csv "1.0.0"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-json "0.5.1"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [compojure "1.6.2"]
                 [environ "1.2.0"]
                 [MPA/mpa-recommender "1.1.0"]]
  :plugins [[lein-environ "1.2.0"]]
  ;:resource-paths ["lib/mpa-recommender-1.0-SNAPSHOT.jar"]
  :repositories {"local" "file:maven"}
  :profiles {:uberjar {:aot :all}}
  :repl-options {:init-ns mpa-recommender-clj.core})
