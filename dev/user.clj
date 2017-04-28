(ns user
  (:require [figwheel-sidecar.repl-api]))

(defn say-hello []
  (println "in my user namespace!"))

(defn start-figwheel! []
  (figwheel-sidecar.repl-api/start-figwheel! "dev" "devcards-test"))

(defn cljs-repl []
  (figwheel-sidecar.repl-api/cljs-repl "dev"))
