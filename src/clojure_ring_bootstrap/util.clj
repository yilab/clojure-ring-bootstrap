(ns clojure-ring-bootstrap.util)

;; Turns out this works for both our urls and zookepeer.
;; Be aware that if these requirements diverge this code
;; will need revision and possibly splitting.
(defn sanitize-path [p]
  (letfn [(add-leading     [s] (if (.startsWith s "/")
                                   s
                                   (str "/" s)))
          (remove-trailing [s] (if (.endsWith s "/")
                                 (apply str (butlast s))
                                 s))]
    (if (not (or (empty? p)
                 (= "/" p)))
      (-> p add-leading remove-trailing)
      "")))
