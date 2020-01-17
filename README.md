# lockjaw


[![Clojars Project](https://img.shields.io/clojars/v/nomnom/lockjaw.svg)](https://clojars.org/nomnom/lockjaw)

[![CircleCI](https://circleci.com/gh/nomnom-insights/nomnom.lockjaw.svg?style=svg)](https://circleci.com/gh/nomnom-insights/nomnom.lockjaw)

<img src="https://vignette.wikia.nocookie.net/marveldatabase/images/5/51/Lockjaw_Vol_1_1_Textless.jpg/revision/latest/scale-to-width-down/670?cb=20171122020841" align="right" height=210 />

Locks, backed by Postgres and Component

## Intro

This is a simple component which uses [Postgres' advisory locks](https://www.postgresql.org/docs/9.6/functions-admin.html#FUNCTIONS-ADVISORY-LOCKS) which are mostly managed by Postgrs itself and are really lightweight.
They are not meant to be used for row level locking, but for implementing concurrency control primitives in applications.

Our intended usage is to ensure that at any given time, only one instance of *something* is doing the work, usually it's for ensuring that only one scheduler at a time is queueing up jobs for periodical processing. See [how it integrates with Eternity](https://github.com/nomnom-insights/nomnom.eternity#with-lock-eternitymiddlewarewith-lock)

## How to use it?

The api is very simple:

- `(lockjaw.protocol/acquire! lock-component)` - acquire the lock
- `(lockjaw.protocol/release! lock-component)` - release the lock (usually not needed, but just in case)

Internally we use a lock ID which is an integer. The lock is established **per connection** (session), meaning the following is true:

- if the current connection holds the lock for given ID, acquiring it again will still hold the lock and return true
- if the current connection doesn't hold the lock for given ID, acquiring it again will return false, *unless the lock was release in the meantime*
- if the connection is stopped, in a clean way (system/component stop) or jvm process exists (crash, or restart) - **the lock is released**

It's important that you use the right connection type, when using tools like pgBouncer, as they might mess with the advisory locks and when they are (not) acquired. [See here for more details](https://electron0zero.xyz/blog/til-connection-pooling-and-pgbouncer).

### Lock IDs

Internally lock ids are just integers, and Lockjaw it easier to create them - we create an id out of a provided `name`.
We use the CRC algorithm for ensuring that given string always produces same integer. Inspired by [Zencoder's Locker library](https://github.com/zencoder/locker/blob/master/lib/locker/advisory.rb#L97-L101).

Note that we only support simple way of locking - by single a ID, it's possible to have to use Postgres locks with a pair of IDs, but it's not supported by Lockjaw (so far).


## Usage


```clojure
(require [lockjaw.core
          [com.stuartsierra.component :as component]
          [utility-belt.sql.component.db-pool :as db-pool]
          lockjaw.protocol :as lock])

(def a-lock
  (.start
   (component/using
   ;; unique id, per service, in 99% of the cases service name is ok
    (lockjaw.core/create {:name "some-service" })
    [:db-conn]))) ;; assumes a hikari-cp pool is here

;; explicitly:
(if (lock/acquire! a-lock)
  (try
    (log/info "doing some work, exclusively")
    (do-work)
    (finally ;; release when done
      (lock/release! a-lock)))
  (log/warn "someone else is doing work"))

  ;;; and with a simple macro:

(lock/with-lock a-lock
  (do-some-work))

;; if the lock is NOT acquired, it will return right away with :lockjaw.operation/no-lock keyword
```



## Mock component

Lockjaw ships with a mock component, which doesn't depend on Postgrs and will always
acquire the lock. It can also be configured to never acquire it:

```clojure

(let [always-lock (lockjaw.mock/create {:always-acquire true})
      never-lock (lockjaw.mock/create {:always-acquire false})]
  (.start always-lock)
  (.start never-lock)
  (lock/acquire! always-lock) ;; => true
  (lock/acquire! never-lock) ;; => false
  (.stop always-lock)
  (.stop never-lock))
```

# Change log

- *unreleased* - 0.2.0-SNAPSHOT, switche to `next.jdbc`
- 2019-10-24 - 0.1.2, Initial public offering

# Roadmap

- [ ] support 2 arg version of the lock API


# Authors

<sup>In alphabetical order</sup>

- [Afonso Tsukamoto](https://github.com/AfonsoTsukamoto)
- [Łukasz Korecki](https://github.com/lukaszkorecki)
- [Marketa Adamova](https://github.com/MarketaAdamova)
