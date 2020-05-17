(ns bookmarks.parser
  (:require [clojure-mail.core :as mail]
            [clojure-mail.message :as message]
            [clojure.tools.reader.edn :as edn]
            [clojure.string :as str]))

(def configuration (edn/read-string (slurp "resources/configuration.edn")))

(defn connect-to-mailserver [configuration] (mail/store
                                              (:protocol configuration)
                                              (:server configuration)
                                              (:username configuration)
                                              (:password configuration)))

(defn open-inbox [connection]
  (mail/inbox connection))

(defn read-mail [mail]
  (message/read-message mail :fields [:from :subject :body]))

(defn empty-subject-to-nil [trimmed-subject]
  (if (empty? trimmed-subject)
    nil
    trimmed-subject))

(defn parse-subject [mail]
  (some-> mail
          (:subject)
          (str/trim)
          (empty-subject-to-nil)
          (str/split #";")
          ))

(defn trim-safely [strings]
  (if (str/blank? strings)
    nil
    (str/trim strings)))

(defn to-lowercase [strings]
  (if (str/blank? strings)
    nil
    (str/lower-case strings)))

(defn parse-bookmark-from-mail [mail]
  (let [interesting-parts (parse-subject mail)]
    {:sender (to-lowercase (trim-safely (:address (first (:from mail)))))
     :link   (trim-safely (get interesting-parts 0))
     :name   (trim-safely (get interesting-parts 1))
     :topic  (to-lowercase (trim-safely (get interesting-parts 2)))}))

(defn trusted-sender? [sender trusted-sender]
  (some #(= sender %) trusted-sender))

(defn valid-sender? [trusted-sender mail]
  (-> (get mail :sender)
      (trusted-sender? trusted-sender)
      (boolean))
  )

(defn valid-link? [bookmark]
  (as-> (get bookmark :link) url
        (or
          (str/starts-with? url "https://")
          (str/starts-with? url "http://")
          )
        )
  )

(defn size [list]
  (println "size: " (count list))
  list
  )

(defn valid-name? [bookmark]
  (as-> (get bookmark :name) name
        (not (str/blank? name))
        )
  )

(defn remove-untrusted-mails [configuration mails]
  (filter (fn [mail]
            (valid-sender?
              (:trusted-sender configuration)
              mail))
          mails)
  )

(defn get-mails [configuration]
  (->> configuration
       connect-to-mailserver
       open-inbox
       (map read-mail)
       (map parse-bookmark-from-mail)
       size
       (filter valid-name?)
       size
       (filter valid-link?)
       size
       (remove-untrusted-mails configuration)
       (map (fn [mail] (dissoc mail :sender)))
       (group-by :topic))
  )
