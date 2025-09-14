(ns dev
  (:require [zappto.core :as c]))

(defn main []
  (c/init!)
  (println "Loaded!"))

(defn ^:dev/after-load reloaded []
  (c/init!)
  (println "Reloaded!"))

(comment
  @c/!store                                                 ; For REPL debugging

  (require '[dataspex.core :as ds])
  (ds/inspect @c/!store)
  )