(ns bookmarks.parser-test
  (:require [clojure.test :refer :all]
            [bookmarks.parser :refer :all]))

(def mail {:from    [{:address "my@address.de", :name "Joscha Harpeng"}],
           :body    {:content-type "text/plain; charset=us-ascii", :body "\r\n"},
           :subject "https://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4;Functional pipelines in Clojure - use pipelines to compose tasks;programming"})
(def uppercase-mail {:from    [{:address "my@address.de", :name "Joscha Harpeng"}],
                     :body    {:content-type "text/plain; charset=us-ascii", :body "\r\n"},
                     :subject "https://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4;Functional pipelines in Clojure - use pipelines to compose tasks;Programming"})
(def spaces-mail {:from    [{:address " my@address.de ", :name " Joscha Harpeng "}],
                  :body    {:content-type "text/plain; charset=us-ascii", :body "\r\n"},
                  :subject " https://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4 ; Functional pipelines in Clojure - use pipelines to compose tasks ; programming "})
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

(def valid-link-mail {:sender "strange@address.de"
                      :link   "http://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4"
                      :name   "Functional pipelines in Clojure - use pipelines to compose tasks"
                      :topic  "programming"})

(def invalid-name-mail {:sender "strange@address.de"
                        :link   "http://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4"
                        :name   ""
                        :topic  "programming"})

(def invalid-link-mail {:sender "strange@address.de"
                        :link   "youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4"
                        :name   "Functional pipelines in Clojure - use pipelines to compose tasks"
                        :topic  "programming"})

(def bookmarks {"python"           [{:link  "https://pbpython.com",
                                     :name  "linkname1",
                                     :topic "python"}
                                    {:link  "https://realpython.com/",
                                     :name  "linkname2",
                                     :topic "python"}],
                "machine learning" [{:link  "https://realpython.com/pandas-python-explore-dataset/",
                                     :name  "linkname3",
                                     :topic "machine learning"}]
                })

(deftest parsing-email
  (testing "parses a mail"
    (is (=
          (parse-bookmark-from-mail mail)
          {:sender "my@address.de"
           :link   "https://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4"
           :name   "Functional pipelines in Clojure - use pipelines to compose tasks"
           :topic  "programming"})))
  (testing "removes spaces"
    (is (=
          (parse-bookmark-from-mail spaces-mail)
          {:sender "my@address.de"
           :link   "https://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4"
           :name   "Functional pipelines in Clojure - use pipelines to compose tasks"
           :topic  "programming"})))
  (testing "puts topic to lowercase"
    (is (=
          (parse-bookmark-from-mail uppercase-mail)
          {:sender "my@address.de"
           :link   "https://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4"
           :name   "Functional pipelines in Clojure - use pipelines to compose tasks"
           :topic  "programming"})))
  (testing "parses mail with missing subject"
    (is (=
          (parse-bookmark-from-mail mail-without-subject)
          {:sender "my@address.de"
           :link   nil
           :name   nil
           :topic  nil})))
  )

(deftest safe-trimming
  (testing "trims nil"
    (is (=
          (trim-safely nil)
          nil)))
  (testing "trims characters"
    (is (=
          (trim-safely " pizza ")
          "pizza"))))


(deftest validating-sender
  (testing "detects valid sender"
    (is (=
          (valid-sender? ["trusted@user.de"] trusted-mail)
          true)))
  (testing "detects invalid sender"
    (is (=
          (valid-sender? ["trusted@user.de"] untrusted-mail)
          false)))
  )

(deftest validating-link
  (testing "detects valid link"
    (is (=
          (valid-link? trusted-mail)
          true))
    (is (=
          (valid-link? valid-link-mail)
          true)))
  (testing "detects invalid link"
    (is (=
          (valid-link? invalid-link-mail)
          false)))
  )

(deftest validating-name
  (testing "detects valid name"
    (is (=
          (valid-name? trusted-mail)
          true)))
  (testing "detects invalid name"
    (is (=
          (valid-name? invalid-name-mail)
          false)))
  )

(deftest parse-and-validate
  (testing "parses and validates a mail"
    (is (=
          (->> [mail]
               (map parse-bookmark-from-mail)
               (filter valid-name?)
               (filter valid-link?)
               (filter (fn [mail] (valid-sender? ["my@address.de"] mail))))
          [{:link   "https://youtu.be/JvGXyNXky0Q?list=PLaSn8eiZ631lrDFmUTBH9LFGzAxc3tvU4"
            :name   "Functional pipelines in Clojure - use pipelines to compose tasks"
            :sender "my@address.de"
            :topic  "programming"}])))
  )
(deftest bookmarks-to-markdown
  (testing "transforms bookmarks to markdown"
    (is (=
          (to-markdown bookmarks)
          "# python\n+ [linkname1](https://pbpython.com)\n+ [linkname2](https://realpython.com/)\n# machine learning\n+ [linkname3](https://realpython.com/pandas-python-explore-dataset/)\n"
          )
        )
    )
  )
