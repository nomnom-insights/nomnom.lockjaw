(ns lockjaw.protocol)


(defprotocol Lockjaw
  (acquire! [this]
    "Tries to get a lock for given component ID")
  (acquire-by-name! [this lock-name]
    "Converts passed name to ID and tries to aquire a lock for it")
  (acquired? [this lock-name]
    "Converts passed name to ID and cheks if lock was already acquired")
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


(defmacro with-named-lock
  "Run the code if a lock with the passed name is obtained"
  [a-lock lock-name & body]
  `(if (acquire-by-name! ~a-lock ~lock-name)
     (do
       ~@body)
     :lockjaw.operation/no-lock))


(defmacro with-named-lock!
  "Like *with-lock* but release it after use"
  [a-lock lock-name & body]
  `(try
     (if (acquire-by-name! ~a-lock ~lock-name)
       (do
         ~@body)
       :lockjaw.operation/no-lock)
     (finally
       (release-by-name! ~a-lock ~lock-name))))
