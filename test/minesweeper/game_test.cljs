(ns minesweeper.game-test
  (:require [cljs.test :refer-macros [is testing async]]
            [devcards.core :refer-macros [deftest]]
            [minesweeper.game :refer [empty-board
                                      place-mines
                                      count-adjacent-mines
                                      label-tiles-with-adjacent-mines]]))

(deftest create-empty-board
  (testing "Initialise an empty board with all tiles unrevealed"
    (let [board (empty-board 3)]
      (is (= 9 (count board)))
      (is (= {:key "0,0"
              :x 0
              :y 0
              :state :unrevealed
              :type nil} (get board "0,0"))))))

(deftest place-mines-on-board
  (testing "randomly placing 5 mines onto a board"
    (let [board (-> (empty-board 3) (place-mines 5))
          tiles-with-mines (filter (fn [[k v]] (= (:type v) :mine)) board)]
      (is (= 9 (count board)))
      (is (= 5 (count tiles-with-mines))))))

(defn place-mine-at [x y board]
  (assoc-in board [(str x "," y) :type] :mine))

(deftest count-adjacent-mines-test
  (testing "mine placed at 3,3"
    (let [board (->> (empty-board 3) (place-mine-at 3 3))]
      (is (= 1 (count-adjacent-mines 3 4 board)))
      (is (= 0 (count-adjacent-mines 3 3 board)))
      (is (= 0 (count-adjacent-mines 0 0 board))))))

(deftest label-tiles-with-number-of-adjacent-mines-2x2
  (testing "write the number of adjacent mines"
    (let [board (->> (empty-board 2) (place-mine-at 0 0) (label-tiles-with-adjacent-mines))
          tile-type (fn [k] (:type (get board k)))]
      (is (= :mine (tile-type "0,0")))
      (is (= :1 (tile-type "0,1")))
      (is (= :1 (tile-type "1,0")))
      (is (= :1 (tile-type "1,1")))
      (is (= 4 (count board))))))

(deftest label-tiles-with-number-of-adjacent-mines-3x3
  (let [board (->> (empty-board 3) (place-mine-at 1 0) (label-tiles-with-adjacent-mines))
        tile-type (fn [k] (:type (get board k)))]

    (testing "top row"
      (is (= :1 (tile-type "0,0")))
      (is (= :mine (tile-type "1,0")))
      (is (= :1 (tile-type "2,0"))))

    (testing "middle row"
      (is (= :1 (tile-type "0,1")))
      (is (= :1 (tile-type "1,1")))
      (is (= :1 (tile-type "2,1"))))

    (testing "bottom row"
      (is (= :0 (tile-type "0,2")))
      (is (= :0 (tile-type "1,2")))
      (is (= :0 (tile-type "2,2"))))

    (is (= 9 (count board)))))
