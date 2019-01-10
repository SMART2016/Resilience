(ns resilienceautomation.core
  (:require [clojure.java.shell :as shell]
            [clojure.tools.logging :as log]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn start-system []
  (log/info "Starting system ...")
  (shell/sh "docker-compose"
            "-p" "Resilience"
            "-f" "docker-compose.yml" "up"
             "-d"
            "--build"
            "--force-recreate"))

(defn stop-system []
   (log/info "Stopping system ...")
   (shell/sh "docker-compose"
             "-p" "Resilience"
             "-f" "docker-compose.yml" "down" "-v" "--remove-orphans"))