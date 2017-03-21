(ns help-me-im-dumb.query-engine
  (:require [help-me-im-dumb.index :as index]))

(declare boolean-query-matcher)

(defn query->results
  [matcher q limit]
  (map index/docid->doc (take limit (matcher (index/sanitize-tokens q)))))

(defn boolean-query-matcher
  "Super dumb query matcher. Returns all docs with all words"
  [q-tokens]
  (if (empty? q-tokens)
    ()
    (apply
     clojure.set/intersection
     (map
      #(into #{} (index/term->postings %))
      q-tokens))))

(def bquery->results (partial query->results boolean-query-matcher))
