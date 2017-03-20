(ns help-me-im-dumb.index
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [spicerack.core :as spicerack]))


(def DICTIONARY-FILENAME "resources/dictionary.txt")
(def POSTINGS-LIST-FILENAME "resources/postings.txt")
(def MAPDB-FILENAME "resources/mapdb.db")
(def POSTINGS-LIST-DB "term-postings-mapping")
(def URL-MAPPING-FILENAME "resources/url-mapping.txt")
(def URL-MAPPING-DB "url-mapping")

(declare create-indicies-helper read-in-dictionary)

(defn initialize-index
  []
  (def DICTIONARY (read-in-dictionary))
  (def MAPDB (spicerack/open-database MAPDB-FILENAME)))

(defn docid->url
  [docid]
  (let [url-map (spicerack/open-hashmap MAPDB URL-MAPPING-DB)]
    (get url-map docid)))

(defn term->postings
  [term]
  (let [plist (let [postings-map (spicerack/open-hashmap MAPDB POSTINGS-LIST-DB)]
                (get postings-map term))]
    (if (some? plist)
      plist
      '())))

(defn create-indicies
  "Main function for creating indicies.
   Reads in datafile, tokenizes, sorts, and writes to files"
  [jsonfile]
  (create-indicies-helper jsonfile))

(defn read-in-dictionary
  []
  (into {} (map #(s/split % #"\t") (line-seq (io/reader DICTIONARY-FILENAME)))))

(defn normalize-tokens
  [tokens]
  (map s/lower-case tokens))

(defn sanitize-tokens
  [tokens]
  (-> tokens
      (clojure.string/replace #"[^a-zA-Z\s]" "")
      (clojure.string/split #"\s+")
      normalize-tokens))

(defn get-tokens-from-doc
  "Takes reddit doc from json data"
  [doc]
  (let [body (doc "body")
        title (doc "title")
        url (doc "url")
        comments (doc "comments")
        comment-string (concat (map #(get % "text") comments))]
    {:url url :title (sanitize-tokens title) :text (sanitize-tokens (apply str body " " comment-string))}))

(defn doc-seq-from-file
  "Returns doc lazy seq from file"
  [json-datafile]
  (map
   (fn [line id]
     (assoc (get-tokens-from-doc (json/read-str line)) :id id))
   (line-seq (io/reader json-datafile))
   (iterate inc 0)))

(defn token-seq-from-docs
  "Returns [id term] from doc-seq"
  [doc-seq]
  (apply
   concat
   (map
    (fn [{id :id tokens :title}]
      (map #(vector id %) tokens))
    doc-seq)))

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
  (with-open [dictionary-file (io/writer DICTIONARY-FILENAME)]
    (let [postings-list-map (spicerack/open-hashmap MAPDB POSTINGS-LIST-DB)]
      (doseq [[term postings] term-postings-mapping]
        (.write dictionary-file
                (str term "\t" (count (distinct postings)) "\n"))
        (spicerack/put! postings-list-map term postings)))))

(defn write-url-mapping-to-file
  [url-seq]
  (let [url-map (spicerack/open-hashmap MAPDB URL-MAPPING-DB)]
    (doseq [[doc-id url] url-seq]
      (spicerack/put! url-map doc-id url))))

(defn create-url-seq
  [doc-seq]
  (map (fn [{id :id url :url}] [id url]) doc-seq))

(defn create-indicies-helper
  "Main function for creating indicies.
   Reads in datafile, tokenizes, sorts, and writes to files"
  [jsonfile]
  (let [docs (doc-seq-from-file jsonfile)
        token-seq (token-seq-from-docs docs)
        url-seq (create-url-seq docs)]
    (-> token-seq ;; Write term postings to file
        create-sorted-term-postings-mapping
        write-term-postings-mapping-to-files)
    (write-url-mapping-to-file url-seq)))
