(ns lockjaw.test-system
  (:require
    [com.stuartsierra.component :as component]
    [utility-belt.sql.component.connection-pool :as cp]))


(def db-spec
  {:pool-name  "test"
   :adapter "postgresql"
   :username (or (System/getenv "POSTGRES_USER") "nomnom")
   :password (or (System/getenv "POSTGRES_PASSWORD") "password")
   :server-name  (or (System/getenv "POSTGRES_HOST") "127.0.0.1")
   :port-number (Integer/parseInt (or (System/getenv "POSTGRES_PORT") "5432"))
   :maximum-pool-size 2
   :database-name (or (System/getenv "POSTGRES_DB") "nomnom_test")})


(defn create
  [extra]
  (component/map->SystemMap
    (merge extra
           {:db-conn (cp/create db-spec)
            :db-conn-2 (cp/create db-spec)})))


(defn start!
  [sysatom extra]
  (reset! sysatom (component/start (create extra))))


(defn stop!
  [sysatom]
  (swap! sysatom component/stop))
