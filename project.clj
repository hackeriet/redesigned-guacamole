(defproject redesigned-guacamole "1.0.0-SNAPSHOT"
  :description "MQTT/Redis/Clojure/Vue experiment"
  :url "http://github.com/hackeriet/redesigned-guacamole"
  :license {:name "Eclipse Public License v1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.2.395"]
                 [org.immutant/web "2.1.5"]
                 [compojure "1.4.0"]
                 [ring/ring-core "1.5.1"]
                 [environ "1.0.0"]
                 [clojurewerkz/machine_head "1.0.0-beta9"]
                 [com.taoensso/carmine "2.15.0"]
                 [cheshire "5.6.3"]
                 [hiccup "1.0.5"]]
  :aot :all
  :main redesigned-guacamole.web
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "redesigned-guacamole-standalone.jar"
  :profiles {:production {:env {:production true}}})
