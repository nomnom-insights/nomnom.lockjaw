(ns lockjaw.mock
  (:require
    [lockjaw.protocol]))


(defrecord LockjawMock
  [always-acquire?]
  lockjaw.protocol/Lockjaw
  (acquire! [_]
    always-acquire?)
  (release! [_]
    always-acquire?)
  (acquire-by-name! [_ _]
    always-acquire?)
  (release-by-name! [_ _]
    always-acquire?))


(defn create
  "Creates a mock, which by default always returns true on acquring the lock.
  use (create {:always-acquire false}) to make it always fail to acquire"
  [options]
  (->LockjawMock (get options :always-acquire true)))
