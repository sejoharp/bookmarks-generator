(ns bookmarks.core
  (:require [clojure-mail.core :as mailcore]
            [clojure-mail.message :as message]
            [clojure.tools.reader.edn :as edn]))

(def configuration (edn/read-string (slurp "resources/configuration.clj")))

(defn store [configuration] (mailcore/store
             (:protocol configuration)
             (:server configuration)
             (:username configuration)
             (:password configuration)))

(defn first-mail [store] (first (take 5 (mailcore/all-messages store "inbox"))))
(defn print-mail [mail] (print (message/read-message mail :fields [:from :subject :body])))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!")
  ;(mailcore/store )
  )
