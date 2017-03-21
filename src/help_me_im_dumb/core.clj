(ns help-me-im-dumb.core
  (:require [help-me-im-dumb.index :as index]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [help-me-im-dumb.handler :refer [run-server]])
  (:gen-class))

(defn usage
  []
  (println "usage: OPTIONS")
  (println "\t-index json-data-file\tIndexes json data file")
  (println "\t-server port\tRuns webserver on given port"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (cond
    (nil? args) (usage)
    (= "-index" (first args))
    (do
      (index/create-indicies (second args))
      (println "Index created successfully!"))
    (= "-server" (first args)) (run-server (read-string (second args)))
    :else (usage)))
