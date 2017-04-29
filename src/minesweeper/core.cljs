(ns minesweeper.core
  (:require [reagent.core :as reagent]
            [minesweeper.view :as view]))

(enable-console-print!)

(reagent/render-component [view/render-game]
                          (. js/document (getElementById "app")))
