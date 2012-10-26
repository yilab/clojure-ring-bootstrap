(defproject clojure-ring-bootstrap "0.2.0-SNAPSHOT"
  :description "Add metrics, healthchecks, and Zookeeper service registration."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [zookeeper-clj "0.9.2" :exclusions [org.clojure/clojure]]
                 [clj-json "0.5.1" :exclusions [org.clojure/clojure]]
                 [cheshire "4.0.1"]
                 [metrics-clojure "0.8.0" :exclusions [cheshire org.clojure/clojure]]
                 [metrics-clojure-ring "0.8.0" :exclusions [cheshire org.clojure/clojure]]
                 [org.clojure/tools.logging "0.2.3"]
                 [log4j/log4j "1.2.16" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [ring/ring-core "1.1.5" :exclusions [org.clojure/clojure]]
                 [hiccup "1.0.0" :exclusions [org.clojure/clojure]]]
  :plugins [[lein-swank "1.4.4"]]
  :dev-dependencies [[ring/ring-jetty-adapter "0.3.11"]])
