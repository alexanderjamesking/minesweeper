(ns minesweeper.view-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard]]
            [minesweeper.game :refer [init-board]]
            [reagent.core :as reagent]
            [minesweeper.game :as game]
            [minesweeper.view :as view]))

(def board-for-testing
  {"0,0" {:type :1    :x 0 :y 0 :state :revealed}
   "1,0" {:type :2    :x 1 :y 0 :state :revealed}
   "2,0" {:type :3    :x 2 :y 0 :state :revealed}
   "3,0" {:type :4    :x 3 :y 0 :state :revealed}
   "0,1" {:type :5    :x 0 :y 1 :state :revealed}
   "1,1" {:type :6    :x 1 :y 1 :state :revealed}
   "2,1" {:type :7    :x 2 :y 1 :state :revealed}
   "3,1" {:type :8    :x 3 :y 1 :state :revealed}
   "0,2" {:type :mine :x 0 :y 2 :state :revealed}
   "1,2" {:type :mine :x 1 :y 2 :state :lost}
   "2,2" {:type :1    :x 2 :y 2 :state :flagged}
   "3,2" {:type :1    :x 3 :y 2 :state :unrevealed}
   "0,3" {:type :0    :x 0 :y 3 :state :revealed}
   "1,3" {:type :0    :x 1 :y 3 :state :revealed}
   "2,3" {:type :0    :x 2 :y 3 :state :revealed}
   "3,3" {:type :0    :x 3 :y 3 :state :revealed}})

(defcard board-with-all-tile-states
  (reagent/as-element (view/render-board board-for-testing)))

(defcard random-board-with-twelve-mines-revealed
  (let [board (->> (game/init-board 9 12)
                   (game/reveal-all))]
    (reagent/as-element (view/render-board board))))

(defcard random-board-with-twelve-mines-unrevealed
  (let [board (->> (game/init-board 9 12))]
    (reagent/as-element (view/render-board board))))
