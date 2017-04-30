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
  (let [generate-coords (juxt (fn north [x y] [x (dec y)])
                              (fn north-east [x y] [(inc x) (dec y)])
                              (fn east [x y] [(inc x) y])
                              (fn south-east [x y] [(inc x) (inc y)])
                              (fn south [x y] [x (inc y)])
                              (fn south-west [x y] [(dec x) (inc y)])
                              (fn west [x y] [(dec x) y])
                              (fn north-west [x y] [(dec x) (dec y)]))]
    (map (fn [[x y]] (str x "," y)) (generate-coords x y))))

(defn- count-adjacent-mines [x y board]
  (let [adj-tiles (select-keys board (adjacent-tiles-coordinates x y))
        adj-mines (filter (fn [[k v]] (= (:type v) :mine)) adj-tiles)]
    (count adj-mines)))

(defn- label-tiles-with-adjacent-mines [board]
  (let [tiles-without-mines (filter (fn [[k v]] (not= :mine (:type v))) board)
        replace-type-with-num-adj-mines (fn [[k v]]
                                          (let [n (count-adjacent-mines (:x v) (:y v) board)]
                                            (assoc {} k (assoc v :type (keyword (str n))))))]
    (->> (mapcat replace-type-with-num-adj-mines tiles-without-mines)
         (into {})
         (merge board))))

(defn init-board [size number-of-mines]
  (-> (empty-board size)
      (place-mines number-of-mines)
      label-tiles-with-adjacent-mines))

(defn reveal-all [board]
  (reduce-kv (fn [m k v] (assoc m k (assoc v :state :revealed))) {} board))

(defn get-adj-tiles-to-reveal [board tile-key]
  (let [tile (get board tile-key)
        adjacent-tiles (select-keys board
                                    (adjacent-tiles-coordinates (:x tile) (:y tile)))
        to-reveal (filter (fn [[k v]] (and (= :0 (:type tile))
                                           (not= :mine (get v :type))
                                           (= :unrevealed (get v :state))))
                          adjacent-tiles)]
    to-reveal))

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
