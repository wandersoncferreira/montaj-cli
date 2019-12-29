(ns montag.core
  (:require [clojure.tools.cli :refer [cli]]
            [montag.http :refer :all :as http]
            [montag.database :refer :all :as database]
            [clojure.pprint :as pp])
  (:gen-class :main true))

(defmulti command (fn [type args banner] type))

(defmethod command :default [type args banner]
  (println banner))

(defmethod command :help [_ _ banner]
  (println banner))

(defmethod command :register
  [_ args _]
  (println (format "The term >>%s<< provided the following titles: " (last args)))
  (let [bookname (last args)
        books (http/search :goodreads :title bookname)
        book-map (user-choice books)]
    (when-not (empty? book-map)
      (database/save->book book-map)
      (database/save->author book-map)
      (println (format "Book %s was saved!" (:book_title book-map))))))

(defmethod command :getter
  [_ args banner]
  (let [ret (database/getter :book (last args))]
    (if (empty? ret)
      (println "No book found!")
      (pp/print-table ret))))

(defmethod command :update
  [_ args banner]
  (let [entity (keyword (last args))
        type (keyword (last (pop args)))
        params (first args)]
    (if (empty? (database/updatter entity type params))
      "Data updated!"
      "Error")))

(defn -main
  [& args]
  (let [[opts args banner] (cli args
                                ["-h" "--help" "Print this help"
                                 :default false :flag true]
                                ["-r" "--register" "Provide portion of book name"]
                                ["-g" "--getter" "Get a list of books saved in the system"
                                 :parse-fn #(keyword %)]
                                ["-u" "--update" "Update the status of the entities"])]
    (database/create->tb-books)
    (database/create->tb-authors)
    (-> #(or (= (second %) true) (string? (second %)) (keyword? (second %)))
        (filter opts)
        first
        (as-> ret
            (command (first ret) (conj args (second ret)) banner)))
    (System/exit 1)))
