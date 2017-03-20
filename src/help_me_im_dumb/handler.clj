(ns help-me-im-dumb.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.adapter.jetty :as jetty]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [help-me-im-dumb.query-engine :as query]))

(defn read-template [filename]
  (io/resource filename))

(defn boolean-query-results
  [q]
  (json/write-str (query/bquery->results q)))

(defroutes app-routes
  (GET "/" [] (read-template "templates/index.html"))
  (GET "/query/:q" [q] (boolean-query-results q))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn run-server
  "Runs jetty server on given port"
  [port]
  (jetty/run-jetty #'app {:port port :join? false}))

;; (def server (run-server 5000))
