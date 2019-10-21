(ns lockjaw.mock
  (:require [lockjaw.protocol]))

(defrecord LockjawMock [result]
  lockjaw.protocol/Lockjaw
  (acquire! [this]
    result)
  (release! [this]
    result))

(defn create
  "Creates a mock, which by default always returns true on acquring the lock.
  use (create {:always-acquire false}) to make it always fail to acquire"
  [options]
  (->LockjawMock (get options :always-acquire true)))
