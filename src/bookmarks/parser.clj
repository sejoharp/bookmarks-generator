(ns bookmarks.parser
  (:require [clojure-mail.core :as mail]
            [clojure-mail.message :as message]
            [clojure.tools.reader.edn :as edn]
            [clojure.string :as str]))

(def configuration (edn/read-string (slurp "resources/configuration.edn")))

(defn connection [configuration] (mail/store
                                   (:protocol configuration)
                                   (:server configuration)
                                   (:username configuration)
                                   (:password configuration)))

(defn inbox-mails [connection]
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

(defn parse-mail [mail]
  (let [interesting-parts (parse-subject mail)]
    {:sender (:address (first (:from mail)))
     :link   (get interesting-parts 0)
     :name   (get interesting-parts 1)
     :topic  (get interesting-parts 2)}))

(defn trusted-sender? [sender trusted-sender]
  (some #(= sender %) trusted-sender))

(defn valid-sender? [mail trusted-sender]
  (-> (get mail :sender)
      (trusted-sender? trusted-sender)
      (boolean))
  )

(defn valid-link? [mail]
  (as-> (get mail :link) element
        (or
          (str/starts-with? element "https://")
          (str/starts-with? element "http://")
          )
        )
  )

(defn valid-name? [mail]
  (as-> (get mail :name) name
      (not (str/blank? name))
      )
  )