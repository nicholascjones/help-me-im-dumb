(ns help-me-im-dumb.query-engine
  (:require [help-me-im-dumb.index :as index]))

(declare boolean-query-matcher)

(defn query->results
  [matcher q limit]
  (map index/docid->url (take limit (matcher q))))

(defn boolean-query-matcher
  "Super dumb query matcher. Returns all docs with all words"
  [q]
  (apply
   clojure.set/intersection
   (map
    #(into #{} (index/term->postings %))
    (clojure.string/split q #"\s+"))))


(def bquery->results (partial query->results boolean-query-matcher))
