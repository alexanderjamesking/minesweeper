(ns minesweeper.game-test
  (:require [cljs.test :refer-macros [is testing async]]
            [devcards.core :refer-macros [deftest]]
            [minesweeper.game :refer [empty-board place-mines count-adjacent-mines]]))

(deftest create-empty-board
  (testing "Initialise an empty board with all tiles unrevealed"
    (let [board (empty-board)]
      (is (= 81 (count board)))
      (is (= {:key "0,0"
              :x 0
              :y 0
              :state :unrevealed
              :type nil} (get board "0,0"))))))

(deftest place-mines-on-board
  (testing "randomly placing 5 mines onto a board"
    (let [board (-> (empty-board) (place-mines 5))
          tiles-with-mines (filter (fn [[k v]] (= (:type v) :mine)) board)]
      (is (= 81 (count board)))
      (is (= 5 (count tiles-with-mines))))))

(defn place-mine-at [x y board]
  (assoc-in board [(str x "," y) :type] :mine))

(deftest count-adjacent-mines-test
  (testing "mine placed at 3,3"
    (let [board (->> (empty-board) (place-mine-at 3 3))]
      (is (= 1 (count-adjacent-mines 3 4 board)))
      (is (= 0 (count-adjacent-mines 3 3 board)))
      (is (= 0 (count-adjacent-mines 0 0 board))))))
