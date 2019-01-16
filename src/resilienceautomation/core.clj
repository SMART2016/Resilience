(ns resilienceautomation.core
    (:gen-class
     :name resilienceautomation.core
     :prefix -
     :main false
     :methods [#^{:static true} [testcase1 [String String] void]
               #^{:static true} [startEnv [String String] void]
               #^{:static true} [destroyEnv [String String] void]])
    (:require [ig.havoc.core :as havoc]
             [resilienceautomation.platform :as plat]
             [clojure.tools.logging :as log]))





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

;;Write testcases with multiple command to disrupt the env as below
(defn -testcase1 [dockerApiUrl envName]
  (testcase1 dockerApiUrl envName)
 )


;;call startEnv to start the env
(defn startEnv [dockerComposeFile envName]
  (plat/start-system dockerComposeFile envName)
)

(defn -startEnv [dockerComposeFile envName]
  (startEnv dockerComposeFile envName)
)

;;call destroyEnv to stop all containers
(defn destroyEnv [dockerComposeFile envName]
  (plat/stop-system dockerComposeFile envName)
)

(defn -destroyEnv [dockerComposeFile envName]
  (destroyEnv dockerComposeFile envName)
)
