(defproject nomnom/lockjaw "0.2.0-SNAPSHOT"
  :description "Postgres Advisory Locks as a Component"
  :url "https://github.com/nomnom-insights/nomnom.lockjaw"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2018
            :key "mit"}
  :deploy-repositories {"clojars" {:sign-releases false
                                   :username :env/clojars_username
                                   :password :env/clojars_password}}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [seancorfield/next.jdbc "1.0.13"]
                 [com.stuartsierra/component "0.4.0"]]
  :plugins [[lein-cloverage "1.0.13" :exclusions [org.clojure/clojure]]]
  :profiles {:dev
             {:resource-paths ["dev-resources"]
              :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                             ;; pulls in all the PG bits and a connection pool
                             ;; component
                             [nomnom/utility-belt.sql "1.0.0-SNAPSHOT"]
                             [org.clojure/tools.logging "0.5.0"]]}})
