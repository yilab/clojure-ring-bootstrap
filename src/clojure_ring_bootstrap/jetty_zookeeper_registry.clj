(ns clojure-ring-bootstrap.jetty-zookeeper-registry
  (:use     clojure-ring-bootstrap.util)
  (:require [zookeeper :as zk]
            [clj-json.core :as json]
            [clojure.tools [logging :as log]])
  (:import  java.net.InetAddress
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
  (let [extra-pairs (if (fn? extras) (extras) extras)]
    (json/generate-string (merge {"hostname" hostname,
                                  "ports" ports
                                  "port" (first ports)} extra-pairs))))

(defn- get-server-ports [^Server server]
  (map #(.getPort %1) (.getConnectors server)))

(defn install-lifecycle-monitor! [^Server server config]
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
                               (try
                                 (let [node-name (zk/create zkc (str path "/provider-")
                                                            :ephemeral?  true
                                                            :sequential? true
                                                            :data       (.getBytes (json/generate-string content)))]
                                   (log/info (str "Registered us into " node-name))
                                   (swap! registered-path (constantly node-name)))
                                 (catch Exception e
                                   (log/error "Could not register our presence with zookeeper." e))))

                             (lifeCycleStopping [event]
                               (when-let [path @registered-path]
                                 (try
                                   (zk/delete zkc path)
                                   (catch Exception e
                                     (log/error "Could not delete presence in zookeeper." e))
                                   (finally (zk/close zkc)))))))))
