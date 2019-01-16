(ns resilienceautomation.platform
  (:require [clojure.java.shell :as shell]
            [ig.havoc.core :as havoc]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop]
            [clojure.test :as t]
            [resilienceautomation.util :as util]
            [clojure.tools.logging :as log]))


(def project-name "resilience")

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

;;This functions help starting the dockerized environment through docker-compose up
(defn start-system [dockerComposeFile projectName]
  (log/info (str "Starting system ..." projectName))
  (shell/sh "docker-compose"
            "-p" projectName
            "-f" dockerComposeFile "up"
             "-d"
            "--build"
            "--force-recreate"))


;;This functions help stopping the dockerized environment through docker-compose down
(defn stop-system [dockerComposeFile projectName]
   (log/info (str "Stopping system ..." projectName))
   (shell/sh "docker-compose"
             "-p" projectName
             "-f" dockerComposeFile "down" "-v" "--remove-orphans"))



;;-------------------Defining Property based testing steps -------------------------------------

(def from [:our-service1 :our-service2])
(def to [:kafka1 :kafka2 :kafka3 :fake])
(def ok {:status :ok})

(def initial-state (into {:kafka1       ok
                          :kafka2       ok
                          :kafka3       ok
                          :our-service1 ok
                          :our-service2 ok}
                         (for [f from
                               t to]
  
                           [[f t] ok])))

(def final-states #{initial-state})

(defn dump-logs []
  (let [dump-file (str "broken." (System/currentTimeMillis) ".log")]
    (log/info "Dumping logs to " dump-file)
    (shell/sh "sh" "-c" (str "docker-compose -p " project-name " logs --no-color >& " dump-file))))

;;Generating command to insert fault
;;havoc/link-gen : fixes/breaks a network connections between the two hosts specified as parameter
;;havoc/link-handicaps-gen: adds network delay, corrupted packets or limits the bandwith between two hosts
;;havoc/container-gen: starts/stops/hangs a container
;;havoc/evil-http-server-gen: returns an infinite response, random response, empty response, rnadom header,
;;                            random content length or a random http status code
(def command-generators
  (concat
    [(havoc/evil-http-server-gen [:fake 3001])]
    (map havoc/container-gen (concat from to))
    (for [f from
          t to]
      (havoc/link-gen [f t]))
    (for [f from
          t to]
      (havoc/link-handicaps-gen [f t]))))



(def at-least-once-property
  (prop/for-all [plan (havoc/random-plan-generator [2 10]
                                                   initial-state
                                                   final-states
                                                   command-generators)]
    (log/info (start-system))
    (try
      (let [docker (havoc/create-docker "http://localhost:2376" project-name)]
        (log/info "The plan is" (havoc/initial->broken plan))
        (util/start-sending! 10000)
        (doseq [cmd (havoc/initial->broken plan)]
          (log/info "Running" cmd)
          (havoc/exec! docker cmd)
          (Thread/sleep 10000))
        (doseq [cmd (havoc/broken->final plan)]
          (log/info "Back to good shape" cmd)
          (havoc/exec! docker cmd))
        (log/info "Waiting for messages to be sent")
        (util/wait-until-sent)
        (let [result
              (util/try-for (* 5 60)
                            (log/info "So far" (count (util/unique-messages)))
                            (= 10000 (count (util/unique-messages))))]
          (when-not result
            (log/warn "Broke!!!" (havoc/initial->broken plan))
            (dump-logs))
          result))
      (finally
        (log/info (stop-system))))))

(defn clean [plan]
  {:initial->broken (havoc/initial->broken plan)
   :broken->final   (havoc/broken->final plan)})

(t/deftest at-least-once
  (let [result (tc/quick-check 100 at-least-once-property)]
    (when-not (true? (:result result))
      (t/is false
            (pr-str (-> result
                        (update-in [:fail 0] clean)
                        ;(dissoc :fail)
                        (update-in [:shrunk :smallest 0] clean)
                        ))))))

;;-------------------------------------------------------------------------------------

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
;;Adds a rule to drop connection to kafka1 server
;;su -c iptables -A INPUT -s 172.28.0.3(kafka1) -j DROP
(havoc/exec! docker
             {:command :link/cut
              :from    :zoo1
              :to      :kafka1})

;;Fixes the connection between zookeeper and kafka instance
;;After running this command you will be able to create topic.
;;Deletes a rule to drop connection to kafka1 server
;;su -c iptables -D INPUT -s 172.28.0.3(kafka1) -j DROP
(havoc/exec! docker
             {:command :link/fix
              :from    :zoo1
              :to      :kafka1})


)