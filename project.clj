(defproject clojure-ring-bootstrap "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [zookeeper-clj "0.9.2" :exclusions [org.clojure/clojure]]
                 [clj-json "0.5.1"]
                 [cheshire "4.0.1"]
                 [metrics-clojure "0.8.0" :exclusions [cheshire]]
                 [metrics-clojure-ring "0.8.0" :exclusions [cheshire]]
                 [org.clojure/tools.logging "0.2.3"]
                 [log4j/log4j "1.2.16" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [ring/ring-core "1.1.5"]
                 [hiccup "1.0.0"]]
  :plugins [[lein-swank "1.4.4"]]
  :dev-dependencies [[ring/ring-jetty-adapter "0.3.11"]])
