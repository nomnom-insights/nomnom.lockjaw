(ns lockjaw.util
  (:import (java.util.zip CRC32)))

(defn name-to-id
  "Converts a string to an int"
  [name]
  (let [bytes (.getBytes ^String name "UTF-8")
        crc (new CRC32)]
    (.update ^CRC32 crc bytes)
    (.getValue ^CRC32 crc)))
