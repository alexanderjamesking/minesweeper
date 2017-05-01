(ns minesweeper.game)

(defn- empty-board [size]
  (into {} (mapcat
            (fn [k] (assoc {} (:key k) k))
            (for [x (range size)
                  y (range size)]
              {:key (str x "," y)
               :x x
               :y y
               :state :unrevealed
               :type nil}))))

(defn- place-mines [board num-mines]
  (merge board (->> board
                    keys
                    shuffle
                    (take num-mines)
                    (select-keys board)
                    (reduce-kv (fn [m k v] (assoc m k (assoc v :type :mine))) {}))))

(defn- adjacent-tiles-coordinates [x y]
  (let [generate-coordinates (juxt (fn north [x y] [x (dec y)])
                                   (fn north-east [x y] [(inc x) (dec y)])
                                   (fn east [x y] [(inc x) y])
                                   (fn south-east [x y] [(inc x) (inc y)])
                                   (fn south [x y] [x (inc y)])
                                   (fn south-west [x y] [(dec x) (inc y)])
                                   (fn west [x y] [(dec x) y])
                                   (fn north-west [x y] [(dec x) (dec y)]))]
    (map (fn [[x y]] (str x "," y))
         (generate-coordinates x y))))

(defn- count-adjacent-mines [x y board]
  (->> (select-keys board (adjacent-tiles-coordinates x y))
       (filter (fn [[k v]] (= (:type v) :mine)))
       count))

(defn- label-tiles-with-adjacent-mines [board]
  (reduce-kv (fn [r k v]
               (if (= :mine (:type v))
                 (assoc r k v)
                 (assoc-in r [k :type]
                           (->> board
                                (count-adjacent-mines (:x v) (:y v))
                                str
                                keyword))))
             board
             board))

(defn init-board [size number-of-mines]
  (-> (empty-board size)
      (place-mines number-of-mines)
      label-tiles-with-adjacent-mines))

(defn reveal-all [board]
  (reduce-kv (fn [m k v] (assoc-in m [k :state] :revealed)) board board))

(defn reveal-all-mines [board]
  (reduce-kv (fn [m k v]
               (if (= :mine (:type v))
                 (assoc-in m [k :state] :revealed)
                 (assoc m k v))) board board))

(defn- get-adj-tiles-to-reveal [board tile-key]
  (let [tile (get board tile-key)]
    (->> (select-keys board (adjacent-tiles-coordinates (:x tile) (:y tile)))
         (filter (fn [[k v]]
                   (and (= :0 (:type tile))
                        (not= :mine (get v :type))
                        (= :unrevealed (get v :state))))))))

(defn reveal-adjacent-empty-tiles [board tile-key]
  (loop [tile-key tile-key
         to-reveal (get-adj-tiles-to-reveal board tile-key)
         new-state (assoc-in board [tile-key :state] :revealed)]
    (if (empty? to-reveal)
      new-state
      (let [next-tile (second (first to-reveal))
            next-key (:key next-tile)
            next-to-reveal (set (concat (rest to-reveal)
                                        (get-adj-tiles-to-reveal new-state next-key)))
            next-state (assoc-in new-state [tile-key :state] :revealed)]
        (recur next-key next-to-reveal next-state)))))

(defn all-non-mine-tiles-revealed? [game-state]
  (let [non-mines (filter (fn [[k v]] (not= :mine (:type v)) ) (-> game-state deref :board))
        unrevealed-count (count (filter (fn [[k v]] (= :unrevealed (:state v))) non-mines))]
    (= 0 unrevealed-count)))

(defn game-lost [game-state tile-key]
  (swap! game-state assoc :board (reveal-all-mines (-> game-state deref :board)))
  (swap! game-state assoc-in [:board tile-key :state] :lost)
  (swap! game-state assoc :game-state :lost))

(defn toggle-flag! [game-state tile-key]
  (let [state (-> game-state deref)
        current-tile-state (get-in state [:board tile-key :state])
        new-state (if (= :unrevealed current-tile-state)
                    :flagged
                    :unrevealed)]
    (swap! game-state assoc-in [:board tile-key :state] new-state)))

(defn reveal-tile! [game-state tile-key]
  (let [state (-> game-state deref)
        clicked-tile (get-in state [:board tile-key])
        reveal-tiles (fn []
                       (swap! game-state assoc :board (reveal-adjacent-empty-tiles (:board @game-state) tile-key))
                       (when (true? (all-non-mine-tiles-revealed? game-state))
                         (swap! game-state assoc :game-state :won)))]
    (case (:type clicked-tile)
      :0 (reveal-tiles)
      :mine (game-lost game-state tile-key)
      (reveal-tiles))))

(defn create-new-game-state [board-size number-of-mines]
  {:board (init-board board-size number-of-mines)
   :game-state :in-progress})
