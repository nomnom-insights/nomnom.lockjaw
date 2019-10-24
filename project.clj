(defproject nomnom/lockjaw "0.1.2"
  :description "Postgres Advisory Locks as a Component"
  :url "https://github.com/nomnom-insights/nomnom.lockjaw"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2018
            :key "mit"}
  :deploy-repositories {"clojars" {:sign-releases false
                                   :username [:gpg :env/clojars_username]
                                   :password [:gpg :env/clojars_password]}}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [nomnom/utility-belt.sql "0.2.2"]
                 [com.stuartsierra/component "0.4.0"]]
  :plugins [[lein-cloverage "1.0.13" :exclusions [org.clojure/clojure]]]
  :profiles {:dev
             {:resource-paths ["dev-resources"]
              :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                             [org.clojure/tools.logging "0.5.0"]]}})
