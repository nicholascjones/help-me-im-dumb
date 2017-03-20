(ns help-me-im-dumb.index
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as s]))

(def DICTIONARY-FILENAME "resources/dictionary.txt")
(def POSTINGS-LIST-FILENAME "resources/postings.txt")

(declare token-seq-from-file create-sorted-term-postings-mapping write-term-postings-mapping-to-files)


(defn read-in-dictionary
  []
  (into {} (map #(s/split % #"\t") (line-seq (io/reader DICTIONARY-FILENAME)))))

(read-in-dictionary)

(create-indicies "resources/test-index")

(defn create-indicies
  "Main function for creating indicies. 
   Reads in datafile, tokenizes, sorts, and writes to files"
  [jsonfile]
  (->> (token-seq-from-file jsonfile)
       create-sorted-term-postings-mapping
       write-term-postings-mapping-to-files))

(defn normalize-tokens
  [tokens]
  (map s/lower-case tokens))

(defn get-tokens-from-doc
  "Takes reddit doc from json data"
  [doc]
  (let [body (doc "body")
        title (doc "title")
        url (doc "url")
        comments (doc "comments")
        comment-string (concat (map #(get % "text") comments))]
    (->
     (apply str body " " comment-string)
     (clojure.string/replace #"[^a-zA-Z\s]" "")
     (clojure.string/split #"\s+")
     normalize-tokens)))

(defn token-seq-from-file
  "Returns [docid token] lazy seq from file"
  [json-datafile]
  (apply
   concat
   (map
    (fn [line id]
      (map #(vector id %) (get-tokens-from-doc (json/read-str line))))
    (line-seq (io/reader json-datafile))
    (iterate inc 0))))

(defn sort-id-token-pairs
  "Sorts id token pairs
   WARNING: Realizes entire sequence"
  [token-id-pairs]
  (sort
   (fn [[id1 token1] [id2 token2]]
     (if (not= 0 (compare id1 id2))
       (compare id1 id2)
       (compare token1 token2)))
   token-id-pairs))

(defn create-sorted-term-postings-mapping
  [id-token-pairs]
  (reduce
   (fn [m [docid token]]
     (update m token
             (fn [ls]
               (if (nil? ls)
                 [docid]
                 (conj ls docid)))))
   (sorted-map)
   id-token-pairs))

(defn write-term-postings-mapping-to-files
  "Writes term and postings list to index files
  
  Dictionary file: term\tdocument_frequency
  Postings list file: term\tpost1;post2;"
  [term-postings-mapping]
  (with-open [dictionary-file (io/writer DICTIONARY-FILENAME)
              postings-list-file (io/writer POSTINGS-LIST-FILENAME)]
    (doseq [[term postings] term-postings-mapping]
      (.write dictionary-file
              (str term "\t" (count (distinct postings)) "\n"))
      (.write postings-list-file
              (str term "\t" (clojure.string/join ";" postings) "\n")))))


