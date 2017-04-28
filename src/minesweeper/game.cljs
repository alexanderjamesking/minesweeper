(ns minesweeper.game)

(defn empty-board []
  (into {} (mapcat
            (fn [k]
              (assoc {} (:key k) k))
            (for [x (range 9)
                  y (range 9)]
              {:key (str x "," y)
               :x x
               :y y
               :state :unrevealed
               :type nil}))))

(defn place-mines [board num-mines]
  (merge board (->> board
                    keys
                    shuffle
                    (take num-mines)
                    (select-keys board)
                    (mapcat (fn [[k v]] (assoc {} k (assoc v :type :mine))))
                    (into {}))))

(defn- n [x y] [x (dec y)])
(defn- ne [x y] [(inc x) (dec y)])
(defn- e [x y] [(inc x) y])
(defn- se [x y] [(inc x) (inc y)])
(defn- s [x y] [x (inc y)])
(defn- sw [x y] [(dec x) (inc y)])
(defn- w [x y] [(dec x) y])
(defn- nw [x y] [(dec x) (dec y)])

(defn- adjacent-tiles-coordinates [x y]
  (let [generate-coords (juxt n ne e se s sw w nw)
        coords (generate-coords x y)]
    (map (fn [[x y]] (str x "," y)) coords)))

(defn- count-adjacent-mines [x y board]
  (let [adjacent-tiles (select-keys board (adjacent-tiles-coordinates x y))
        adjacent-mines (filter (fn [[k v]] (= (:type v) :mine)) adjacent-tiles)]
    (count adjacent-mines)))
