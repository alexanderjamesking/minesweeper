(ns minesweeper.view
  (:require [minesweeper.game :as game]
            [reagent.core :as reagent :refer [atom]]))

(defn initial-app-state []
  {:board (game/init-board 9 10)})

(defonce app-state
  (atom (initial-app-state)))

(def rect-size 50)
(def rect-stroke 1)

(def number-to-colour
  {:1 "#2980B9"
   :2 "#27AE60"
   :3 "#C0392B"
   :4 "purple"
   :5 "#BA4A00"
   :6 "#52BE80"
   :7 "#117A65"
   :8 "#D35400"})

(defn mine-svg [fill]
  (let [inner-rect-size (- rect-size (* rect-stroke 2))
        border [:rect {:style {:fill "#D7DBDD"}
                       :width rect-size
                       :height rect-size}]
        inner-rect [:rect {:style {:fill fill}
                           :width inner-rect-size
                           :height inner-rect-size
                           :x rect-stroke
                           :y rect-stroke}]]
    [:g
     border
     inner-rect
     [:line {:x1 25 :y1 7 :x2 25 :y2 42 :stroke-width 3 :stroke "black"}]
     [:line {:x1 7 :y1 25 :x2 42 :y2 25 :stroke-width 3 :stroke "black"}]
     [:line {:x1 12 :y1 12 :x2 37 :y2 37 :stroke-width 3 :stroke "black"}]
     [:line {:x1 37 :y1 12 :x2 12 :y2 37 :stroke-width 3 :stroke "black"}]
     [:circle {:cx 25 :cy 25 :r 12}]
     [:circle {:style {:fill "white"} :cx 22 :cy 22 :r 3}]]))

(defn mine []
  (mine-svg "#E5E8E8"))

(defn mine-lost []
  (mine-svg "red"))

(defn revealed [tile-type]
  (let [inner-rect-size (- rect-size (* rect-stroke 2))
        border [:rect {:style {:fill "#D7DBDD"}
                       :width rect-size
                       :height rect-size}]
        inner-rect [:rect {:style {:fill "#E5E8E8"}
                           :width inner-rect-size
                           :height inner-rect-size
                           :x rect-stroke
                           :y rect-stroke}]
        text (case tile-type
               :0 nil
               [:text {:style {:user-select "none"
                               :cursor "default"
                               :font-family "Helvetica, Arial"
                               :font-weight 300}
                       :x 17
                       :y 35
                       :text-length 5
                       :font-size 26
                       :fill (get number-to-colour tile-type)} (name tile-type)])]
    [:g border inner-rect text]))

(defn render-tile [tile]
  (reagent/as-element [:svg {:width rect-size :height rect-size}
                       (revealed tile)]))

(defn unrevealed []
  (let [inner-rect-padding 3
        inner-rect-size (- rect-size (* inner-rect-padding 2))
        triangle-size (- rect-size 1)
        triangle-left [:polygon {:style {:fill "#E5E7E9"}
                                 :points (str "1," triangle-size " " triangle-size ",1 1,1")}]
        triangle-right [:polygon {:style {:fill "#979A9A"}
                                  :points (str triangle-size ",1 1," triangle-size " " triangle-size "," triangle-size)}]
        inner-rect [:rect {:style {:fill "#D0D3D4"
                                   :cursor "pointer"}
                           :width inner-rect-size
                           :height inner-rect-size
                           :x inner-rect-padding
                           :y inner-rect-padding}]
        border [:rect {:style {:fill "#D7DBDD"}
                       :width rect-size
                       :height rect-size}]]
    [:g border triangle-left triangle-right inner-rect]))

(defn flagged []
  (let [inner-rect-padding 3
        inner-rect-size (- rect-size (* inner-rect-padding 2))
        triangle-size (- rect-size 1)
        triangle-left [:polygon {:style {:fill "#E5E7E9"
                                         :cursor "pointer"}
                                 :points (str "1," triangle-size " " triangle-size ",1 1,1")}]
        triangle-right [:polygon {:style {:fill "#979A9A"
                                          :cursor "pointer"}
                                  :points (str triangle-size ",1 1," triangle-size " " triangle-size "," triangle-size)}]

        inner-rect [:rect {:style {:fill "#D0D3D4"
                                   :cursor "pointer"}
                           :width inner-rect-size
                           :height inner-rect-size
                           :x inner-rect-padding
                           :y inner-rect-padding}]

        border [:rect {:style {:fill "#D7DBDD"}
                       :width rect-size
                       :height rect-size}]]
    [:g
     border
     triangle-left
     triangle-right
     inner-rect
     [:rect {:style {:fill "black"
                     :cursor "pointer"}
             :x 29
             :y 14
             :width 3
             :height 24}]
     [:rect {:style {:fill "black"
                     :cursor "pointer"}
             :x 25
             :y 36
             :height 3
             :width 10}]
     [:polygon {:style {:fill "#D30831"
                        :cursor "pointer"}
                :points "15,20 32,10 32,30"}]

     [:polygon {:style {:fill "#E20934"
                        :cursor "pointer"}
                :points "17,20 29,13 29,27"}]]))

(defn click-handler [k]
  (let [state (-> app-state deref)
        clicked-tile (get-in state [:board k])
        current (if (= :0 (:type clicked-tile))
                  {:board (game/reveal-adjacent-empty-tiles (:board state) (:key clicked-tile))}

                  (assoc-in state [:board k :state] :revealed))]

    ;; check tile type
    ;; if its a mine - they lose
    ;; if it's :0 then reveal surrounding tiles

    (reset! app-state current)))

(defn render-board [board]
  (let [board-size (* rect-size (Math/sqrt (count board)))]
    [:svg {:width board-size :height board-size}
     (map (fn [[k v]]
            (let [x (:x v)
                  y (:y v)
                  transform (str "translate(" (* rect-size x) "," (* rect-size y) ")")]
              [:g {:transform transform
                   :key k
                   :on-click (partial click-handler k)
                   }

               (case (:state v)
                 :revealed (case (:type v)
                             :mine (mine)
                             (revealed (:type v)))
                 :unrevealed (unrevealed)
                 :flagged (flagged)
                 :lost (mine-lost))]))
          board)]))

(defn render-game []
  [:div
   [:button {:on-click #(reset! app-state (initial-app-state))} "reset"]
   [:button {:on-click #(reset! app-state {:board (game/reveal-all (-> app-state deref :board))})} "reveal all"]

   [:div
    (render-board (-> app-state deref :board))
    ]])
