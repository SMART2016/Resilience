(defproject resilienceautomation "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-modules "0.3.11"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 [org.slf4j/log4j-over-slf4j "1.7.14"]
                 [org.slf4j/jul-to-slf4j "1.7.14"]
                 [org.slf4j/jcl-over-slf4j "1.7.14"]
                 [ig/havoc "0.1.1"]
                 [org.clojure/tools.logging "0.1.2"]])
