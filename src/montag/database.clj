(ns montag.database
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :as cstr]
            [montag.http :refer :all :as http]
            [clojure.set :refer [rename-keys]])
  (:gen-class))

(def db-name (str (System/getenv "HOME") "/.montag/db/database.sqlite"))

(defn execute-query [& args]
  (apply sh "sqlite3" "-quote" "-header" "-separator" " | " db-name args))

(defn create->tb-books []
  (let [query "CREATE TABLE IF NOT EXISTS books (
id bigint PRIMARY KEY,
title varchar(200) NOT NULL,
reviews_count int,
publication_year int,
average_rating float,
author_id int,
read boolean default false
);"]
    (execute-query query)))

(defn create->tb-authors []
  (let [query "CREATE TABLE IF NOT EXISTS authors (
id bigint PRIMARY KEY,
name varchar(200)
);"]
    (execute-query query)))

(defn text-val [s]
  (format "\"%s\"" s))

(defn int-val [i]
  (format "%s" i))

(defn make-row [& xs]
  (format "(%s)" (cstr/join ", " xs)))

(defn- parse-book [book]
  (-> book
      (select-keys [:book_id :book_title :text_reviews_count :original_publication_year :author_id :average_rating])
      (rename-keys {:book_id :id
                    :book_title :title
                    :text_reviews_count :reviews_count
                    :author_id :author_id
                    :average_rating :average_rating
                    :original_publication_year :publication_year})
      (update :id biginteger)
      (update :reviews_count biginteger)
      (update :publication_year biginteger)
      (update :author_id biginteger)
      (update :average_rating read-string)))

(defn make-query [book table-name]
  (let [values (vals book)
        columns (keys book)]
    (str (format "insert into %s (" table-name)
         (cstr/join "," (map name columns)) ") values "
         (apply make-row (map #(if (string? %) (text-val %)
                                   (int-val %)) values)))))

(defn- parse-author [book]
  (-> book
      (select-keys [:author_id :author_name])
      (rename-keys {:author_id :id
                    :author_name :name})
      (update :id biginteger)))

(defn save->book [book]
  (-> book
      parse-book
      (make-query "books")
      execute-query))

(defn save->author [book]
  (-> book
      parse-author
      (make-query "authors")
      execute-query))

(defmulti getter (fn [type params] [type params]))

(def getter-query "select bk.id, title, reviews_count as reviews, publication_year, average_rating as rating, read, at.name as author
from books as bk inner join authors as at on bk.author_id=at.id")

(defmethod getter [:book :all]
  [_ _]
  (let [ret (:out (execute-query getter-query))
        parsed-data (->> (cstr/replace ret #"'" "")
                         (cstr/split-lines)
                         (map #(cstr/split % #",")))]
    (->> parsed-data
         rest
         (map #(zipmap (first parsed-data) %)))))

(defmethod getter [:book :read]
  [_ _]
  (let [ret (:out (execute-query (str getter-query " where bk.read = 1")))
        parsed-data (->> (cstr/replace ret #"'" "")
                         (cstr/split-lines)
                         (map #(cstr/split % #",")))]
    (->> parsed-data
         rest
         (map #(zipmap (first parsed-data) %)))))


(defmulti updatter (fn [entity type params] [entity type]))

(defmethod updatter [:book :read]
  [_ _ bookid]
  (let [query (str "update books set read = 1 where id=" bookid)
        ret (:out (execute-query query))]
    ret))
