(ns resilienceautomation.core
   (:require [ig.havoc.core :as havoc]
            [resilienceautomation.platform :as plat]
            [clojure.tools.logging :as log]))


;;Write testcases with multiple command to disrupt the env as below
(defn testcase1 [dockerApiUrl envName]
   ;;Controller to control individual docker containers in the above environment
  ;;Note: the project name in the docker var definition must be all small case
	(let [docker (havoc/create-docker dockerApiUrl envName)]
  (havoc/exec! docker
             {:command :container/stop
              :host    :cassandra1})
  (havoc/exec! docker
             {:command :container/start
              :host    :cassandra1})
  (havoc/exec! docker
             {:command :link/cut
              :from    :zoo1
              :to      :kafka1})
  (havoc/exec! docker
             {:command :link/fix
              :from    :zoo1
              :to      :kafka1})
  )
 )


;;call startEnv to start the env
(defn startEnv [dockerComposeFile envName]
  (plat/start-system dockerComposeFile envName)
)

;;call destroyEnv to stop all containers
(defn destroyEnv [dockerComposeFile envName]
  (plat/stop-system dockerComposeFile envName)
)



