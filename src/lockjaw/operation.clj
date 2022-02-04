(ns lockjaw.operation
  (:refer-clojure :exclude [key])
  (:require
    [next.jdbc :as jdbc]))


(def acquire-lock-query "SELECT pg_try_advisory_lock(?)")

(def release-lock-query "SELECT pg_advisory_unlock(?)")

(def release-all-locks-query "SELECT pg_advisory_unlock_all()")

(def find-lock-for-id-query "SELECT objid, mode from pg_locks WHERE locktype = 'advisory' and  objid = ?")

(def find-all-locks "SELECT * from pg_locks WHERE locktype = 'advisory'")


;; The registry ensures that:
;; - we keep a track of how many times given lock has been acquired
;; - we can use it to release all locks on the JVM shutdown
;; - we can inspect if a lock is actually held in given instance

(def no-lock ::no-lock)


(def registry
  (atom {}))


(defn- inc-key
  [key reg]
  (update reg key #(inc (or % 0))))


(defn- dec-key
  [key reg]
  (update reg key #(dec (or % 0))))


(defn acquire-lock
  [db-conn lock-id]
  (let [res (-> (jdbc/execute-one! db-conn [acquire-lock-query lock-id])
                :pg_try_advisory_lock
                true?)]
    (when res
      (swap! registry (partial inc-key lock-id)))
    res))


(defn release-lock
  [db-conn lock-id]
  (let [res (-> (jdbc/execute-one! db-conn [release-lock-query lock-id])
                :pg_advisory_unlock
                true?)]
    (when res
      (swap! registry (partial dec-key lock-id)))
    res))


(defn lock-acquired?
  [db-conn lock-id]
  (boolean (jdbc/execute-one! db-conn [find-lock-for-id-query lock-id])))


(defn release-all-locks!
  "Releases all locks hold by this connection, regardless of how many were acquired"
  [db-conn]
  (jdbc/execute-one! db-conn [release-all-locks-query])
  (reset! registry {}))


(defn all-locks
  [db-conn]
  (jdbc/execute! db-conn [find-all-locks]))
