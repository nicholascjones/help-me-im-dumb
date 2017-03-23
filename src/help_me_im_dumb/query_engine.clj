(ns help-me-im-dumb.query-engine
  (:require [help-me-im-dumb.index :as index]))

(declare boolean-query-matcher tifdf-matcher-helper)

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

(defn idf-score
  "for a given docs and a term find idf"
  [term]
  (/
    (float index/TOTAL-DOCS)
    (float (read-string (index/DICTIONARY term)))))

(defn tf-score
  "given 1 doc and 1 term find term freq"
  [term docid]
  1)

(defn tfidf-scores
  [docids term]
  (map
   (fn [docid]
     (vector
      docid
      (* (tf-score term docid) (idf-score term))))
   (distinct docids)))

(defn tfidf-scores-for-query
  [q-tokens]
  (apply
   concat
   (map tfidf-scores (map index/term->postings q-tokens) q-tokens)))

(defn tfidf-matcher
  "Returns results sorted by tfidf"
  [q-tokens]
  (map
   first
   (sort-by
    #(* -1 (second %))
    (reduce
     (fn [m [docid score]]
       (update m docid #(+ score (or % 0))))
     {}
     (tfidf-scores-for-query q-tokens)))))

(def tfidfquery->results (partial query->results tfidf-matcher))

(defn list-all-docs
  "for a given query list all documents that at least one appears in"
  [q]
  (apply clojure.set/union
         (distinct
           (map
             #(into #{} (index/term->postings %))
             (clojure.string/split q #"\s+")
             ))))

