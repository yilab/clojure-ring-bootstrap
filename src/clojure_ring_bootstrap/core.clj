(ns clojure-ring-bootstrap.core
  (:use [clojure-ring-bootstrap [healthcheck :only (healthcheck-middleware)]]
        [metrics.ring [expose :only (expose-metrics-as-json)]
                      [instrument :only (instrument)]]))


(defn- sanitize-path [p]
  (letfn [(prefix [p] (if (.startsWith p "/")
                        p
                        (str "/" p)))
          (suffix [p] (if (.endsWith p "/")
                        (apply str (butlast p))
                        p))]
    (-> p prefix suffix)))


(defn bootstrap-middleware [handler & {:keys [healthchecks healthcheck-endpoint metrics-endpoint] :or
                                       {healthchecks {}, healthcheck-endpoint "/healthcheck",
                                         metrics-endpoint "/metrics" }}]
  (let [hpoint (sanitize-path healthcheck-endpoint)
        mpoint (sanitize-path metrics-endpoint)]
    (-> handler
        instrument
        (expose-metrics-as-json mpoint)
        (healthcheck-middleware healthchecks hpoint))))
