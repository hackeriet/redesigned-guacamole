(ns redesigned-guacamole.web
  (:use [hiccup.page :only (html5 include-css include-js)])
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.core.async :as async :refer [chan go sliding-buffer put! <!]]
            [immutant.web             :as web]
            [immutant.web.async       :as webasync]
            [immutant.web.middleware  :as web-middleware]
            ;[cheshire.core :refer :all]
            [environ.core :refer [env]]
            [clojurewerkz.machine-head.client :as mh]
            [taoensso.carmine :as car :refer (wcar)])
  (:gen-class))

;; A channel for chromecast events and a mult to copy messages to all websocket
;; listeners.
;; This probably needs to be pub/sub to support multiple topics in the future
(def songs-chan (chan (sliding-buffer 10)))
(def songs-mult (async/mult songs-chan))

;; Redis
(def redis-conn {:pool {} :spec {:uri (env :redis-url)}})
(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

;; Chromecast HTML template, includes a bit of play history from elems
(defn songs [elems]
  (html5 [:head
          [:title "Hackeriets sick beats"]
          (include-css "css/materialize.min.css")
          (include-css "css/songs.css")
          ;(include-css "https://fonts.googleapis.com/icon?family=Material+Icons")
          (include-js "https://unpkg.com/vue/dist/vue.js")
          [:script {:type "text/javascript"}
           (concat "const data=[" (clojure.string/join "," elems) "]")]
          (include-js "js/songs.js")
          ;(include-js "js/jquery-3.1.1.min.js")
          ;(include-js "js/materialize.min.js")]
         [:body
          [:div {:class "container"}
           [:div {:class "col s12 m4"}
            [:transition-group {:name "effect" :tag "ul" :id "songs" :class "collection"}
             [:li {:class "collection-item avatar" :v-for "song in songs" :v-bind:key "song" :v-if "song.title"}
              [:img {:v-if "song.images" :class "circle" (symbol ":src") "song.images[0].url"}]
              [:span {:class "title"} "{{song.title}}"]
              [:p
               "{{song.artist}}" [:br]
               "{{song.title}}"]
              ;[:a {:href "#!" :class "secondary-content"}
              ; [:i {:class "medium material-icons"} "play_circle_outline"]]
              ]]]]]))

;; Return chromecast page
(defn chromecast-songs [topic]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (songs
          (map
           (fn [x] (String. x))
           (wcar* (car/lrange topic 0 100))))})

;; Web routes
(defroutes app
  (GET "/" []
       (chromecast-songs "hackeriet/chromecast"))
  (GET "/ping" []
       (println "ping")
       {:body "PONG"})
  (route/resources "/")
  (route/not-found (slurp (io/resource "404.html"))))

;; Subscription callback, stores in redis and pushes to websockets if chromecast
(defn mqtt-to-redis [^String topic _ ^String payload]
  (wcar* (car/lpush topic payload))
  (if (= "hackeriet/chromecast" topic)
    (put! songs-chan (String. payload "UTF-8"))))

;; On websocket open tap into mult and send it down the socket forever
(def websocket-callbacks
  {:on-open   (fn [channel]
                (let [s (chan)]
                  (async/tap songs-mult s)
                  (async/go-loop []
                    (webasync/send! channel (<! s))
                    (recur))))
   :on-close   (fn [channel {:keys [code reason]}]
                 (println "close code:" code "reason:" reason))})

(defn -main [& [port]]
  ;; MQTT connection
  (let [mqtt (mh/connect (env :mqtt-url) (mh/generate-id)
                         {:username (env :mqtt-user)
                          :password (env :mqtt-pass)})]
    (mh/subscribe mqtt {"hackeriet/#" 0} mqtt-to-redis))
  (let [port (Integer. (or (env :port) port 5000))]
    (web/run
      (-> app
          (web-middleware/wrap-session)
          (web-middleware/wrap-websocket websocket-callbacks))
      {:port port :host "0.0.0.0"})))
