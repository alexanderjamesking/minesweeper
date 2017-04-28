(ns minesweeper.core
  (:require [reagent.core :as reagent]
            [minesweeper.view :as view]))

(enable-console-print!)

(reagent/render-component [view/render-board]
                          (. js/document (getElementById "app")))
