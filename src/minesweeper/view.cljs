(ns minesweeper.view
  (:require [minesweeper.game :as game]
            [reagent.core :as reagent :refer [atom]]))

(def board-size 9)
(def number-of-mines 10)

(defonce app-state
  (atom (game/create-new-game-state board-size number-of-mines)))

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

(defn unrevealed [clicking-enabled?]
  (let [inner-rect-padding 3
        inner-rect-size (- rect-size (* inner-rect-padding 2))
        triangle-size (- rect-size 1)
        triangle-left [:polygon {:style {:fill "#E5E7E9"}
                                 :points (str "1," triangle-size " " triangle-size ",1 1,1")}]
        triangle-right [:polygon {:style {:fill "#979A9A"}
                                  :points (str triangle-size ",1 1," triangle-size " " triangle-size "," triangle-size)}]
        inner-rect [:rect {:style (merge {:fill "#D0D3D4"}
                                         (when clicking-enabled? {:cursor "pointer"}))
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

(defn left-click-handler [tile-key]
  (game/reveal-tile! app-state tile-key))

(defn right-click-handler [tile-key click-event]
  (game/toggle-flag! app-state tile-key)
  (.preventDefault click-event))

(defn render-board [board clicking-enabled?]
  (let [board-size (* rect-size (Math/sqrt (count board)))]
    [:svg {:width board-size :height board-size}
     (map (fn [[k v]]
            (let [x (:x v)
                  y (:y v)
                  state (:state v)
                  transform (str "translate(" (* rect-size x) "," (* rect-size y) ")")]
              [:g (merge  {:transform transform
                           :key k}
                          (if (or (= :flagged state) (= :unrevealed state))
                            {:on-context-menu (partial right-click-handler k)}
                            {:on-context-menu #(.preventDefault %)}) ; no right click for revealed tiles
                          ;; no left click for flagged tiles
                          (when (and clicking-enabled? (not= :flagged state))
                            {:on-click (partial left-click-handler k)}))
               (case (:state v)
                 :revealed (case (:type v)
                             :mine (mine)
                             (revealed (:type v)))
                 :unrevealed (unrevealed clicking-enabled?)
                 :flagged (flagged)
                 :lost (mine-lost))])) board)]))

(defn reset-game []
  (reset! app-state (game/create-new-game-state board-size number-of-mines)))

(defn reveal-all []
  (reset! app-state {:board (game/reveal-all (-> app-state deref :board))}))

(defn render-game []
  [:div
   [:button {:on-click reset-game} "reset"]
   [:button {:on-click reveal-all} "reveal all"]

   [:h2 (str "Game state: " (-> app-state deref :game-state))]

   [:div
    (render-board (-> app-state deref :board) (= :in-progress (-> app-state deref :game-state)))]])
