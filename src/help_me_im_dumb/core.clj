(ns help-me-im-dumb.core
  (:require [help-me-im-dumb.index :as index])
  (:gen-class))

(defn usage
  []
  (println "usage: OPTIONS")
  (println "\t-index json-data-file\tIndexes json data file"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (cond
    (nil? args) (usage)
    (= "-index" (first args))
    (do
      (index/create-indicies (first args))
      (println "Index created successfully!"))
    :else (usage)))
