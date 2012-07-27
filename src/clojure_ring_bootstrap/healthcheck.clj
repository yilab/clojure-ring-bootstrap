(ns clojure-ring-bootstrap.healthcheck
  (:use [ring.util.response :only [response status content-type]]
        [hiccup.core :only (html)]
        [hiccup.page :only (html5)]))

(defn- healthcheck-result [check]
  (try
    (let [check-result (check)]
      (if check-result
        [::success check-result]
        [::failure check-result]))
    (catch Exception e
      [::failure e])))

(defn- all-succeded? [healthcheck-results]
  (empty? (filter #(not (= ::success (first (second %)))) healthcheck-results)))

(defn- run-healthchecks [healthcheck-map]
  (into {} (map (fn [[name check]] [name (healthcheck-result check)]) healthcheck-map)))

(defn- result->html [[check-name [status result]]]
  (list [:dt.check_name (name check-name)]
        [:dd.check_result (if (= status ::success)
                            [:span.success "success"]
                            [:span.failure (str "failed! Result: "
                                                (.toString result))])]))

(defn- html-healthcheck-results [healthcheck-results]
  (html5 [:header [:title "Healthchecks"]]
         [:section [:p "The following healthchecks have been registered:"]
          [:dl (map result->html healthcheck-results)]]))

(defn- healthcheck-action [named-healthchecks]
  (let [results    (run-healthchecks named-healthchecks)
        succeeded? (all-succeded? results)]
    (-> (response (html-healthcheck-results results))
        (content-type "text/html")
        (status (if succeeded? 200 500)))))


(defn healthcheck-middleware [handler named-healtchecks handler-path]
  (fn [request]
    (if (.startsWith (:uri request) handler-path)
      (healthcheck-action named-healtchecks)
      (handler request))))
