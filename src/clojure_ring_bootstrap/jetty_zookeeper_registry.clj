(ns clojure-ring-bootstrap.jetty-zookeeper-registry
  (:require [zookeeper :as zk]
            [clj-json.core :as json])
  (:import java.net.InetAddress
           (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.util.component AbstractLifeCycle$AbstractLifeCycleListener)))

(defn- config->zk-connection [config]
  (let [server-string (or (:zk-connect config) "localhost:2181")]
    (zk/connect server-string)))

(defn- config->hostname [config]
  (let [strategy (or (:hostname config)
                     (constantly (.getHostName (InetAddress/getLocalHost))))]
    (if (fn? strategy)
      (strategy)
      strategy)))

(defn- node-content [hostname ports extras]
  (json/generate-string (merge {"hostname" hostname,
                                "ports" ports} extras)))

(defn- get-server-ports [^Server server]
  (map #(.getPort %1) (.getConnectors server)))

(defn sanitize-path [p]
  (letfn [(add-leading     [s] (if (.startsWith s "/")
                                   s
                                   (str "/" s)))
          (remove-trailing [s] (if (.endsWith s "/")
                                 (apply str (butlast s))
                                 s))]
    (-> p add-leading remove-trailing)))

(defn- install-lifecycle-monitor! [^Server server config]
  (let [zkc      (config->zk-connection config)
        path     (sanitize-path (or (:path config) "/"))
        hostname (config->hostname config)
        ports    (get-server-ports server)
        extra-content (or (:contents config) {})
        content  (node-content hostname ports extra-content)
        registered-path (atom nil)]
    (.setStopAtShutdown server true)
    (.addLifeCycleListener server
                           (proxy [AbstractLifeCycle$AbstractLifeCycleListener] []
                             (lifeCycleStarted [event]
                               (println "Loading up the lifecycle")
                               (try
                                 (let [node-name (zk/create zkc (str path "/provider-")
                                                            :ephemeral?  true
                                                            :sequential? true
                                                            :data       (.getBytes (json/generate-string content)))]
                                   (swap! registered-path (constantly node-name)))
                                 (catch Exception e
                                   (println "Derped out\n")
                                   (.printStackTrace e)
                                   ;; TODO: Figure out a solution here.
                                   )))
                             (lifeCycleStopping [event]
                               (when-let [path @registered-path]
                                 (zk/delete registered-path)))))))

(defn zk-service-configurator [& {:as config}]
  (fn [server]
    (install-lifecycle-monitor! server config)
    server))
