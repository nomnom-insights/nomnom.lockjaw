(ns lockjaw.operation-test
  (:require [clojure.test :refer :all]
            [utility-belt.sql.component.connection-pool :as cp]
            [lockjaw.test-system :as test-system]
            [lockjaw.operation :as operation]))

(def system (atom nil))

(use-fixtures :each (fn [test-fn]
                      (test-system/start! system {})
                      (test-fn)
                      (test-system/stop! system)))

(deftest lock-operations
  (testing "one conn acquires lock, other doesnt"
    (is (operation/acquire-lock (:db-conn @system) 13))
    (is (= 1
           (count (operation/all-locks (:db-conn @system)))))
    (is (= 1
           (get @operation/registry 13)))
    (is (not (operation/acquire-lock (:db-conn-2 @system) 13))))
  (testing "releases lock, nobody hs it"
    (is (operation/release-lock (:db-conn @system) 13))
    (is (nil? (seq (operation/all-locks (:db-conn @system)))))))

(deftest continous-acquiring-and-relesing
  (testing "it can continously acquire the lock and it's ok"
    (is (operation/acquire-lock (:db-conn @system) 27))
    (is (operation/acquire-lock (:db-conn @system) 27))
    (is (operation/acquire-lock (:db-conn @system) 27))
    (is (operation/acquire-lock (:db-conn @system) 27))
    (is (= 4
           (get @operation/registry 27)))
    (is (= [27]
           (map :pg_locks/objid (operation/all-locks (:db-conn @system)))))
    (operation/release-lock (:db-conn @system) 27)
    (is (= 3
           (get @operation/registry 27)))
    (is (= [27]
           (map :pg_locks/objid (operation/all-locks (:db-conn @system)))))
    (operation/release-all-locks! (:db-conn @system))
    (is (= nil
           (get @operation/registry 27)))
    (is (= []
           (map :pg_locks/objid (operation/all-locks (:db-conn @system)))))))
