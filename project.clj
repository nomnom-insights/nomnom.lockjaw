(defproject nomnom/lockjaw "0.3.1"
  :description "Postgres Advisory Locks as a Component"
  :url "https://github.com/nomnom-insights/nomnom.lockjaw"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2018
            :key "mit"}
  :deploy-repositories {"clojars" {:sign-releases false
                                   :username :env/clojars_username
                                   :password :env/clojars_password}}

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [com.github.seancorfield/next.jdbc "1.2.761"]
                 [com.stuartsierra/component "1.0.0"]]
  :profiles {:dev
             {:resource-paths ["dev-resources"]
              :dependencies [[ch.qos.logback/logback-classic "1.2.10"]
                             ;; pulls in all the PG bits and a connection pool
                             ;; component
                             [nomnom/utility-belt.sql "1.1.0"]
                             [org.clojure/tools.logging "1.2.4"]]}})
