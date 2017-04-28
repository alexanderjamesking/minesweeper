(ns minesweeper.view
  (:require [minesweeper.game :as game]
            [reagent.core :as reagent :refer [atom]]))

(defn initial-app-state []
  {:board (game/init-board 9 10)})

(defonce app-state
  (atom (initial-app-state)))

(def rect-size 50)
(def rect-stroke 1)
(def light-grey "#FDFCFD")
(def dark-grey "#757575")
(def med-grey "#EAEDED")

(def number-to-colour
  {:1 "#2980B9"
   :2 "#27AE60"
   :3 "#C0392B"
   :4 "purple"
   :5 "maroon"
   :6 "#52BE80"
   :7 "#117A65"
   :8 "#D35400"})

(defn text-node [t]
  (case t
    :mine [:text {:x 12
                  :y 48
                  :text-length 50
                  :font-size 50
                  :fill "black"} "*"]
    :0 nil
    [:text {:x 17
            :y 35
            :text-length 5
            :font-size 26
            :fill (get number-to-colour t)} (name t)]))

(defn revealed [tile]
  (let [inner-rect-size (- rect-size (* rect-stroke 2))
        border [:rect {:style {:fill "#D7DBDD"}
                       :width rect-size
                       :height rect-size}]
        inner-rect [:rect {:style {:fill "#E5E8E8"}
                           :width inner-rect-size
                           :height inner-rect-size
                           :x rect-stroke
                           :y rect-stroke}]]
    [:g border inner-rect (text-node (:type tile))]))

(defn unrevealed []
  (let [inner-rect-padding 3
        inner-rect-size (- rect-size (* inner-rect-padding 2))
        triangle-left [:polygon {:style {:fill "#E5E7E9"}
                                 :points (str "0," rect-size " " rect-size ",0 0,0")}]
        triangle-right [:polygon {:style {:fill "#979A9A"}
                                  :points (str rect-size ",0 0," rect-size " " rect-size "," rect-size)}]
        inner-rect [:rect {:style {:fill "#D0D3D4"}
                           :width inner-rect-size
                           :height inner-rect-size
                           :x inner-rect-padding
                           :y inner-rect-padding}]
        ]
    [:g triangle-left triangle-right inner-rect]))

(defn click-handler [k]
  (let [state (-> app-state deref)
        current (assoc-in state [:board k :state] :revealed)]
    (reset! app-state current)))

(defn render-board []
  [:div
   [:button {:on-click #(reset! app-state (initial-app-state))} "reset"]

   [:div
    [:svg {:width 450 :height 450}
     (map (fn [[k v]]
            (let [x (:x v)
                  y (:y v)
                  transform (str "translate(" (* rect-size x) "," (* rect-size y) ")")]
              [:g {:transform transform
                   :key k
                   :on-click (partial click-handler k)}

               (if (= :revealed (:state v))
                 (revealed v)
                 (unrevealed))]))
          (-> app-state deref :board))]]])
