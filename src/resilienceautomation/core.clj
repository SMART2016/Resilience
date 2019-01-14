(ns resilienceautomation.core
  (:require [clojure.java.shell :as shell]
            [clojure.tools.logging :as log]
            [clj-http.client :as http-client]
            [ig.havoc.core :as havoc]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

;;This functions help starting the dockerized environment through docker-compose up
(defn start-system []
  (log/info "Starting system ...")
  (shell/sh "docker-compose"
            "-p" "Resilience"
            "-f" "docker-compose.yml" "up"
             "-d"
            "--build"
            "--force-recreate"))


;;This functions help stopping the dockerized environment through docker-compose down
(defn stop-system []
   (log/info "Stopping system ...")
   (shell/sh "docker-compose"
             "-p" "Resilience"
             "-f" "docker-compose.yml" "down" "-v" "--remove-orphans"))


(comment
(start-system)

;;Controller to control individual docker containers in the above environment
;;Note: the project name in the docker var definition must be all small case
	(def docker (havoc/create-docker "http://localhost:4243" "resilience"))
	
;;Command to stop the zookeeper service	
(havoc/exec! docker
             {:command :container/stop
              :host    :zoo1})

;;Command to start the zookeeper service	
(havoc/exec! docker
             {:command :container/start
              :host    :zoo1})

;;Command to start the zookeeper service	
(havoc/exec! docker
             {:command :container/stop
              :host    :cassandra1})

(havoc/exec! docker
             {:command :container/start
              :host    :cassandra1})

;;Breaks the connection between zookeeper and kafka instance
;;Check by trying to create a topic on kafka it will not be able to create
(havoc/exec! docker
             {:command :link/cut
              :from    :zoo1
              :to      :kafka1})

;;Fixes the connection between zookeeper and kafka instance
;;After running this command you will be able to create topic.
(havoc/exec! docker
             {:command :link/fix
              :from    :zoo1
              :to      :kafka1})


)



