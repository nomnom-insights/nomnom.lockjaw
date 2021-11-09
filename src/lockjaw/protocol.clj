(ns lockjaw.protocol)

(defprotocol Lockjaw
  (acquire! [this]
    "Tries to get a lock for given component ID")
  (acquire-by-name! [this lock-name]
    "Converts passed name to ID and tries to aquire a lock for it")
  (release! [this]
    "Rleases the component lock")
  (release-by-name! [this lock-name]
    "Converts passed name to ID and releases it")
  (release-all! [this]
    "Releases all acquired locks"))

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
