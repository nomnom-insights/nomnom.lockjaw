(ns lockjaw.test-system
  (:require [utility-belt.sql.component.connection-pool :as cp]
            [com.stuartsierra.component :as component]))

(def db-spec
  {:pool-name  "test"
   :adapter "postgresql"
   :username (or (System/getenv "POSTGRES_USER") "nomnom")
   :password (or (System/getenv "POSTGRES_PASSWORD") "password")
   :server-name  (or (System/getenv "POSTGRES_HOST") "127.0.0.1")
   :port-number (or (System/getenv "POSTGRES_HOST") "5432")
   :maximum-pool-size 2
   :database-name (or (System/getenv "POSTGRES_DB") "nomnom_test")})

(defn create [extra]
  (component/map->SystemMap
   (merge extra
          {:db-conn (cp/create db-spec)
           :db-conn-2 (cp/create db-spec)})))

(defn start! [sysatom extra]
  (reset! sysatom (component/start (create extra))))

(defn stop! [sysatom]
  (swap! sysatom component/stop))
