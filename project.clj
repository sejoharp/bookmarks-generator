(defproject bookmarks "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [io.forward/clojure-mail "1.0.8"]
                 [org.clojure/tools.reader "0.10.0"]
                 [marge "0.16.0"]
                 ]
  :repl-options {:init-ns bookmarks.core})
