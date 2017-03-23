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



(defn list-all-docs
  "for a given query list all documents that at least one appears in"
  [q]
  (apply clojure.set/union
         (distinct
           (map
             #(into #{} (index/term->postings %))
             (clojure.string/split q #"\s+")
             ))))


(defn idf-score
  "for a given docs and a term find idf"
  [term docs]
  (/
    (float (count docs))
    (float (count (index/term->postings term)))
    )
  )


(defn tf-score
  "given 1 doc and 1 term find term freq"
  [term docid]
  (count
    (re-seq(re-pattern term)
            (get (index/docid->doc docid) :title))))

