(ns clojure-getting-started.web
  (:use [hiccup.page :only (html5 include-css include-js)])
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [immutant.web             :as web]
            [immutant.web.async       :as async]
            [immutant.web.middleware  :as web-middleware]
            [environ.core :refer [env]]
            [clojurewerkz.machine-head.client :as mh]
            [taoensso.carmine :as car :refer (wcar)]
            [cheshire.core :refer :all]))
;; MQTT
(def mqtt (mh/connect (env :mqtt-url) (mh/generate-id)
                      {:username (env :mqtt-user)
                       :password (env :mqtt-pass)}))
;; Redis
(def redis-conn {:pool {} :spec {:uri (env :redis-url)}})
(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(defn songs [elems]
  (html5 [:head
          [:title "Hackeriets sick beats"]
          (include-js "js/songs.js")
          (include-css "css/songs.css")]
         [:body
          [:h1 "Hello"]
          (for [x elems]
            (let [info (parse-string x true)]
              [:li (:title info)]))]))

(defn chromecast-songs [topic]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (songs
          (map
           (fn [x] (String. x))
           (wcar* (car/lrange topic 0 100))))})

(defroutes app
  (GET "/" []
       (chromecast-songs "hackeriet/chromecast"))
  (ANY "*" []
       (route/resources "/")
       (route/not-found (slurp (io/resource "404.html")))))

;; Subscription callback, stores in redis
(defn mqtt-to-redis [^String topic _ ^String payload]
  (wcar* (car/lpush topic payload)))

(def websocket-callbacks
  "WebSocket callback functions"
  {:on-open   (fn [channel]
                (async/send! channel "Ready for things!"))
   :on-close   (fn [channel {:keys [code reason]}]
                 (println "close code:" code "reason:" reason))
   :on-message (fn [ch m]
                 (async/send! ch (apply str (reverse m))))})

(defn -main [& [port]]
;  (mh/subscribe mqtt {"hackeriet/+" 0} mqtt-to-redis)
  (let [port (Integer. (or port (env :port) 5000))]
    (web/run
      (-> app
          (web-middleware/wrap-session)
          (web-middleware/wrap-websocket websocket-callbacks))
      {"port" port})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
