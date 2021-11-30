(ns lockjaw.core
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [lockjaw.operation :as operation]
    [lockjaw.protocol :as lockjaw]
    [lockjaw.util :as util]))


(defrecord Lockjaw
  [name lock-id db-conn]

  component/Lifecycle

  (start
    [this]
    (let [lock-id (util/name-to-id name)]
      (log/infof "name=%s status=starting lock-id=%s" name lock-id)
      (assoc this :lock-id lock-id)))


  (stop
    [this]
    (log/warnf "name=%s status=stopping lock-id=%s cleaning all locks!" name lock-id)
    (operation/release-all-locks! db-conn)
    (assoc this :lock-id nil))


  lockjaw/Lockjaw

  (acquire!
    [_]
    (operation/acquire-lock db-conn lock-id))


  (release!
    [_]
    (operation/release-lock db-conn lock-id)))


(defn create
  [{:keys [name] :as args}]
  {:pre [(and (string? name) (not (.isEmpty ^String name)))]}
  (map->Lockjaw args))
