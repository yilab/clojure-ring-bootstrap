(ns clojure-ring-bootstrap.core
  (:use clojure-ring-bootstrap.util
        [clojure-ring-bootstrap [healthcheck :only (healthcheck-middleware)]
         [jetty-zookeeper-registry :only (install-lifecycle-monitor!)]]
        [metrics.ring [expose :only (expose-metrics-as-json)]
         [instrument :only (instrument)]]))


(defn bootstrap-middleware [handler & {:keys [healthchecks healthcheck-endpoint metrics-endpoint] :or
                                       {healthchecks {}, healthcheck-endpoint "/healthcheck",
                                         metrics-endpoint "/metrics" }}]
  (let [hpoint (sanitize-path healthcheck-endpoint)
        mpoint (sanitize-path metrics-endpoint)]
    (-> handler
        instrument
        (expose-metrics-as-json mpoint)
        (healthcheck-middleware healthchecks hpoint))))

(defn zk-service-configurator [& {:keys [path hostname contents zk-connect] :as config}]
  (fn [server]
    (install-lifecycle-monitor! server config)
    server))
