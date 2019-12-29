(ns montag.core
  (:require [clojure.tools.cli :refer [cli]]
            [montag.http :refer :all :as http]
            [montag.database :refer :all :as database]
            [clojure.pprint :as pp])
  (:gen-class :main true))

(defmulti command (fn [type args banner] type))

(defmethod command :default [type args banner]
  banner)

(defmethod command :help [_ _ banner]
  banner)

(defmethod command :register
  [_ bookname _]
  (let [books (http/search :goodreads :title bookname)
        book-map (user-choice books)]
    (when-not (empty? book-map)
      (database/save->book book-map)
      (database/save->author book-map)
      (println (format "Book %s was saved!" (:book_title book-map))))))

(defmethod command :getter
  [_ args banner]
  (let [ret (database/getter :books args)]
    (pp/print-table ret)))

(defn -main
  [& args]
  (let [[opts args banner] (cli args
                                ["-h" "--help" "Print this help"
                                 :default false :flag true]
                                ["-r" "--register" "Provide portion of book name"
                                 :flag true]
                                ["-g" "--getter" "Get a list of books saved in the system"
                                 :parse-fn #(keyword %)])]
    (database/create->tb-books)
    (database/create->tb-authors)
    (-> #(or (= (second %) true) (string? (second %)) (keyword? (second %)))
        (filter opts)
        ffirst
        (command args banner))
    (System/exit 1)))
