(ns zappto.router
  (:require [clojure.string :as s]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.history :as rfh]))

;; Define routes with their names and views
(def routes
  [["/"
    {:name :route/homepage
     :view :pages/homepage-view}]

   ["/services"
    {:name :route/services
     :view :pages/services-view}]

   ["/team"
    {:name :route/team
     :view :pages/team-view}]

   ["/calendar"
    {:name :route/calendar
     :view :pages/calendar-view}]

   ["/info"
    {:name :route/info
     :view :pages/info-view}]])

;; Create the router instance
(def router
  (rf/router routes {}))

(defn ignore-anchor-click? [router event anchor-element uri]
  ;;(js/console.log "router: " router "\nevent: " event "\nelement: " elem "\nuri: " uri)
  (and
    (rfh/ignore-anchor-click? router event anchor-element uri)
    (s/starts-with? (.-href anchor-element) "#")))

(defn init-router!
  "Initialize the router with HTML5 history and start it."
  [on-navigate-fn]
  (rfe/start!
    router
    on-navigate-fn
    {:use-fragment         false
     :ignore-anchor-click? ignore-anchor-click?}))

(defn navigate!
  "Navigate to the specified route."
  [route-name & [params]]
  (rfe/push-state route-name params))

;; Match a URL path to a route
(defn match-by-path
  "Find a route match for a given URL path."
  [path]
  (rf/match-by-path router path))

;; Match a route by name
(defn match-by-name
  "Find a route match for a given route name and optional params."
  [route-name & [params]]
  (rf/match-by-name router route-name params))