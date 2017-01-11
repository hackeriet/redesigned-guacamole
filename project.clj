(defproject clojure-getting-started "1.0.0-SNAPSHOT"
  :description "Demo Clojure web app"
  :url "http://clojure-getting-started.herokuapp.com"
  :license {:name "Eclipse Public License v1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.immutant/web "2.1.5"]
                 [compojure "1.4.0"]
                 [ring/ring-core "1.4.0"]
                 [environ "1.0.0"]
                 [clojurewerkz/machine_head "1.0.0-beta9"]
                 [com.taoensso/carmine "2.15.0"]
                 [cheshire "5.6.3"]
                 [hiccup "1.0.5"]]
  :main clojure-getting-started.web
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"] [lein-ring "0.10.0"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "clojure-getting-started-standalone.jar"
  :ring {:handler clojure-getting-started.web/app}
  :profiles {:production {:env {:production true}}})
