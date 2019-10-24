(ns lockjaw.util-test
  (:require [clojure.test :refer :all]
            [lockjaw.util :as util]))

(def sample-names
  ["optimus-prime"
   "megatron"
   "skywarp"
   "lazerbeak"
   "unicron"])

(deftest ensures-all-names-from-id-are-unique
  (testing "name to id is always the same"
    (is (= 3092707704
           (util/name-to-id "sunstreak")))
    (is (= 3903148020
           (util/name-to-id "blitz")))
    (is (= 3076615343
           (util/name-to-id "blitzwing"))))
  (testing "generated ids are always unique"
    (is (= (count sample-names)
           (count
            (set
             (map util/name-to-id sample-names)))))))
