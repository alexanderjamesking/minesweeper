(ns minesweeper.view
  (:require [minesweeper.game :as game]
            [minesweeper.graphics :as graphics]
            [reagent.core :as reagent :refer [atom]]))

(def board-size 9)
(def number-of-mines 10)

(defonce app-state
  (atom (game/create-new-game-state board-size number-of-mines)))

(def rect-size 50)

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
                  transform (str "translate("
                                 (* graphics/rect-size x)
                                 ","
                                 (* graphics/rect-size y)
                                 ")")]
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
                             :mine (graphics/mine)
                             (graphics/revealed (:type v)))
                 :unrevealed (graphics/unrevealed clicking-enabled?)
                 :flagged (graphics/flagged)
                 :lost (graphics/mine-lost))])) board)]))

(defn reset-game []
  (reset! app-state (game/create-new-game-state board-size number-of-mines)))

(defn reveal-all []
  (reset! app-state {:board (game/reveal-all (-> app-state deref :board))}))

(defn render-game []
  [:div {:style {:width 450}}
   [:div {:style {:display "flex"
                  :align-items "center"
                  :justify-content "center"}}
    [:div {:style {:align-self "flex-start"}}]

    [:div {:on-click reset-game
           :style {:max-width "50%"
                   :cursor "pointer"}}
     (case (-> app-state deref :game-state)
       :in-progress [graphics/smiley]
       :lost [graphics/sad]
       :won [graphics/cool])]

    [:div {:style {:align-self "flex-end"}}]]

   [:div
    (render-board (-> app-state deref :board)
                  (= :in-progress (-> app-state deref :game-state)))]])
