(defproject clojure-ring-bootstrap "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [zookeeper-clj "0.9.1"]
                 [clj-json "0.5.1"]
                 [cheshire "4.0.1"]
                 [metrics-clojure "0.8.0" :exclusions [cheshire]]
                 [metrics-clojure-ring "0.8.0" :exclusions [cheshire]]
                 [ring/ring-core "1.1.1"]
                 [hiccup "1.0.0"]]
  :plugins [[lein-swank "1.4.4"]]
  :dev-devependencies [])
