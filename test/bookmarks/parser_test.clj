(ns bookmarks.parser-test
  (:require [clojure.test :refer :all]
            [bookmarks.parser :refer :all]))

(def mail {:from    [{:address "my@address.de", :name "Joscha Harpeng"}],
           :body    {:content-type "text/plain; charset=us-ascii", :body "\r\n"},
           :subject "https://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4;Functional pipelines in Clojure - use pipelines to compose tasks;programming"})
(def mail-without-subject {:from    [{:address "my@address.de", :name "Joscha Harpeng"}],
                           :body    {:content-type "text/plain; charset=us-ascii", :body "\r\n"},
                           :subject ""})
(def trusted-mail {:sender "trusted@user.de"
                   :link   "https://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4"
                   :name   "Functional pipelines in Clojure - use pipelines to compose tasks"
                   :topic  "programming"})

(def untrusted-mail {:sender "strange@address.de"
                     :link   "https://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4"
                     :name   "Functional pipelines in Clojure - use pipelines to compose tasks"
                     :topic  "programming"})

(deftest parsing-email
  (testing "parses a mail"
    (is (=
          (parse-mail mail)
          {:sender "my@address.de"
           :link   "https://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4"
           :name   "Functional pipelines in Clojure - use pipelines to compose tasks"
           :topic  "programming"})))
  (testing "parses mail with missing subject"
    (is (=
          (parse-mail mail-without-subject)
          {:sender "my@address.de"
           :link   nil
           :name   nil
           :topic  nil})))
  )

(deftest validating-sender
  (testing "detects valid sender"
    (is (=
          (valid-mail? trusted-mail ["trusted@user.de"])
          true)))
  (testing "detects invalid sender"
    (is (=
          (valid-mail? untrusted-mail ["trusted@user.de"])
          false)))
  )