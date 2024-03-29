(ns lockjaw.core-test
  (:require [clojure.test :refer :all]
            [lockjaw.test-system :as ts]
            [com.stuartsierra.component :as component]
            [lockjaw.protocol :as lock]
            [lockjaw.core]))

(def sys (atom nil))

(use-fixtures :each (fn [test-fn]
                      (ts/start! sys
                                 {:lock-1 (component/using
                                           (lockjaw.core/create {:name "lock-1"})
                                           [:db-conn])
                                  :lock-2 (component/using
                                           (lockjaw.core/create {:name "lock-1"})
                                           {:db-conn :db-conn-2})})
                      (test-fn)
                      (ts/stop! sys)))

(deftest component-usage
  (testing "it generates a int lock id"
    (is (number? (:lock-id (:lock-1 @sys))))
    (is (number? (:lock-id (:lock-2 @sys))))
    (is (=
         (:lock-id (:lock-2 @sys))
         (:lock-id (:lock-1 @sys))))
    (is (= 3379800295
           (:lock-id (:lock-1 @sys)))))
  (testing "lock-1 gets a lock, lock-2 doesnt"
    (is (lock/acquire! (:lock-1 @sys)))
    (is (false? (lock/acquire! (:lock-2 @sys))))
    (is (lock/release! (:lock-1 @sys)))))

(deftest a-handy-macro
  (testing "nice macro ensures lock clean up"
    (let [fut (future
                (lock/with-lock (:lock-1 @sys)
                  (Thread/sleep 50)
                  ::done))]
      (Thread/sleep 10)
      (is (= :lockjaw.operation/no-lock
             (lock/with-lock! (:lock-2 @sys)
               ::invalid)))
      (is (= ::done
             @fut)))))
