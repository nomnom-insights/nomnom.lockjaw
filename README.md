# lockjaw


[![Clojars Project](https://img.shields.io/clojars/v/nomnom/lockjaw.svg)](https://clojars.org/nomnom/lockjaw)

[![CircleCI](https://circleci.com/gh/nomnom-insights/nomnom.lockjaw.svg?style=svg)](https://circleci.com/gh/nomnom-insights/nomnom.lockjaw)

<img src="https://vignette.wikia.nocookie.net/marveldatabase/images/5/51/Lockjaw_Vol_1_1_Textless.jpg/revision/latest/scale-to-width-down/670?cb=20171122020841" align="right" height=210 />

Locks, backed by Postgres and Component

## Intro

Lockjaw is a simple [Component](https://github.com/stuartsierra/component) which uses [Postgres' advisory locks](https://www.postgresql.org/docs/9.6/functions-admin.html#FUNCTIONS-ADVISORY-LOCKS) which are managed by Postgrs itself and are really lightweight.
They are not meant to be used for row level locking, but for implementing concurrency control primitives in applications.

Intended usage is to ensure that at any given time, only one instance of *something* is doing the work, usually it's for ensuring that only one scheduler at a time is queueing up jobs for periodical processing. See [how it integrates with Eternity](https://github.com/nomnom-insights/nomnom.eternity#with-lock-eternitymiddlewarewith-lock)

### Locking advice

Ideally, you do not do much work while holding the lock. E.g. ensuring that your application pushed only 1 job to a queue or call an endpoint once. Relying on performing long running tasks while holding the lock is not advised. Use it as a coordination mechanism, not the business logic.

## How to use it?

The usage boils down to:

- creating a lock instance with a lock name `(def lock-component (lockjaw.core/create {:name "delete-account"}))`
- `(lockjaw.protocol/acquire! lock-component)` - acquire the lock
- `(lockjaw.protocol/release! lock-component)` - release the lock (usually not needed, but just in case)

Internally we use a lock ID which is an integer. The lock is established **per connection** (session), meaning the following is true:

- if the current connection holds the lock for given ID, acquiring it again will still hold the lock and return true
- if the current connection doesn't hold the lock for given ID, attempting to acquire it  will return false, *unless the lock was released in the meantime*
- if the connection is stopped, in a clean way (system/component stop) or jvm process exits (crash, or restart) - **the lock is released**

It's important that you use the right connection type, especially when using tools like pgBouncer, as they might mess with the advisory locks and when they are (not) acquired. [See here for more details](https://electron0zero.xyz/blog/til-connection-pooling-and-pgbouncer).

### Lock IDs

On Postgres level lock ids are just integers, and Lockjaw makes it easier to create them - we create an id out of a provided `name` configuration option.
We use the CRC algorithm for ensuring that given string always produces same integer. Inspired by [Zencoder's Locker library](https://github.com/zencoder/locker/blob/master/lib/locker/advisory.rb#L97-L101).


### "Dynamic" locks

While you can set the lock name while creating the component, you might need to dynamically create locks to ensure that only single user account is being processed at a given time. In that case you cannot create a component for each user ID, so you will need to use the following functions to acquire per-ID locks:


- create your component (see above)
- use `(lockjaw.protocol/acquire-by-name! lock (str "user:" (-> user :id)))` to hold a lock for given user id
- use `(lockjaw.protocol/acquire-by-name! lock (str "user:" (-> user :id)))` to release it

Same caveats apply as to 'default' locks: do not hold them for too long, and use them  as a coordination mechanism instead.

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
    [:db-conn]))) ;; assumes a hikari-cp pool is here, can be any other JDBC Postgres driver though!

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


;;; 'dynamic' locking

(lock/with-named-lock! (:a-lock component) "delete-account:1"
  (do-delete component {:account-id 1 }))
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

  ;; you can also pass the name lock name:
  (lock/acquire-by-name! always-lock "who?") ;; => true
  (.stop always-lock)
  (.stop never-lock))
```

# Testing

Just run:

`lein test`

> Ensure no other database connections are currently holding advisory locks when running tests.

# Change log

- 2022-02-08 - 0.3.1 - adds "acquired?" functions to check if a lock was already acquired. Updates dependencies (next.jdbc, logback-classic, tools.logging)
- 2021-12-08 - 0.3.0 - "dynamic" locks, updated dependencies
- 2021-11-09 - 0.2.1-SNAPSHOT, updates dependencies, includes `next.jdbc`, allows passing lock-name when asking for lock.
- *unreleased* - 0.2.0-SNAPSHOT, switches to `next.jdbc`
- 2019-10-24 - 0.1.2, Initial public offering

# Authors

<sup>In alphabetical order</sup>

- [Afonso Tsukamoto](https://github.com/AfonsoTsukamoto)
- [Łukasz Korecki](https://github.com/lukaszkorecki)
- [Marketa Adamova](https://github.com/MarketaAdamova)
