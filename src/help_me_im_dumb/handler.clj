(ns help-me-im-dumb.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.adapter.jetty :as jetty]
            [clojure.java.io :as io]))

(defn read-template [filename]
  (io/resource filename))

(defroutes app-routes
  (GET "/" [] (read-template "templates/index.html"))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn run-server
  "Runs jetty server on given port"
  [port]
  (jetty/run-jetty #'app {:port port :join? false}))
