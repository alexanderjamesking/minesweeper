(ns minesweeper.core
  (:require [reagent.core :as reagent]
            [minesweeper.app :as app]))

(defn main []
  (enable-console-print!)
  (println "Hello, World!"))

(reagent/render-component [app/hello-world]
                          (. js/document (getElementById "app")))
