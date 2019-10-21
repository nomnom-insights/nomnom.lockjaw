(ns lockjaw.protocol)

(defprotocol Lockjaw
  (acquire! [this]
    "Tries to get a lock for given ID")
  (release! [this]
    "Rleases the lock"))

(defmacro with-lock
  "Run the code if a lock is obtained"
  [a-lock & body]
  `(if (acquire! ~a-lock)
     (do
       ~@body)
     :lockjaw.operation/no-lock))

(defmacro with-lock!
  "Like *with-lock* but release it after use"
  [a-lock & body]
  `(try
     (if (acquire! ~a-lock)
       (do
         ~@body)
       :lockjaw.operation/no-lock)
     (finally
       (release! ~a-lock))))
