(ns montag.http
  (:require [clj-http.lite.client :as client]
            [clojure.pprint :as pp]
            [clojure.string :as cstr]
            [clojure.data.xml :refer [parse-str]])
  (:gen-class))

(def goodreads-key "AW8AqT4TtbKXk4MhION6tQ")

(def goodreads-base-url (format "https://www.goodreads.com/search/index.xml?key=%s&q=" goodreads-key))

(str goodreads-base-url "brave and true")
;; => "https://www.goodreads.com/search/index.xml?key=AW8AqT4TtbKXk4MhION6tQ&q=brave and true"

(defn- parse-single-book [book]
 (-> (for [[k, v] (map (juxt :tag :content) (:content book))
           :let [fmt-v (if (not= k :best_book) (first v)
                           [:book_id (first (:content (first v)))
                            :book_title (first (:content (second v)))
                            :author_id (first (:content (first (:content (nth v 2)))))
                            :author_name (first (:content (second (:content (nth v 2)))))])]]
       [k fmt-v])
     (as-> ret (merge (into {} ret) (apply hash-map (second (first (filter #(= :best_book (first %)) ret))))))
     (dissoc :id :best_book :original_publication_day :original_publication_month :ratings_count :books_count)))

(defprotocol BookSearch
  "Interface to represent the result of a consult in the book search service."
  (max-hits [this] "Number max of items found")
  (last-page [this] "Last page with results found")
  (books [this] "List of books found")
  (display [this] "Display pretty table for users")
  (user-choice [this] "Choose which book the user is looking for"))

(def common-parse (comp :content second :content))

(defrecord GoodReadsXML []
  BookSearch
  (max-hits [this] (-> this
                       common-parse
                       (nth 3)
                       :content
                       first
                       biginteger))
  (last-page [this] (-> this
                        common-parse
                        (nth 2)
                        :content
                        first
                        biginteger))
  (books [this] (-> this
                    :content
                    second
                    :content
                    (nth 6)
                    :content
                    (as-> ret (map parse-single-book ret))))
  (display [this] (let [ret (books this)]
                    (->> ret
                         (map #(dissoc % :author_id :text_reviews_count))
                         pp/print-table)))
  (user-choice [this] (do
                        (display this)
                        (println "\n \n")
                        (println "Insert the book_id of the correct one: ")
                        (let [ret (read-line)]
                          (first (filter #(= (:book_id %) ret) (books this)))))))


(defmulti search (fn [provider parameter args] [provider parameter]))

(defmethod search [:goodreads :title]
  [_ _ book-name]
  (let [url (str goodreads-base-url (cstr/replace book-name #" " "+"))]
    (-> url
        client/get
        :body
        parse-str
        (map->GoodReadsXML))))
