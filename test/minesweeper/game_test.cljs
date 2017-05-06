(ns minesweeper.game-test
  (:require [cljs.test :refer-macros [is testing async]]
            [devcards.core :refer-macros [deftest defcard]]
            [minesweeper.game :as game]
            [minesweeper.view :as view]
            [reagent.core :as reagent]))

(def clicking-enabled true)

(defn- place-mine-at [x y board]
  (assoc-in board [(str x "," y) :type] :mine))

(defn- select-tile-keys [board state]
  (-> (filter (fn [[k v]] (= state (:state v))) board)
      keys
      sort))

(deftest create-empty-board
  (testing "Initialise an empty board with all tiles unrevealed"
    (let [board (game/empty-board 3)]
      (is (= 9 (count board)))
      (is (= {:key "0,0"
              :x 0
              :y 0
              :state :unrevealed
              :type nil} (get board "0,0"))))))

(deftest place-mines-on-board
  (testing "Randomly placing 5 mines onto a board"
    (let [board (-> (game/empty-board 3)
                    (game/place-mines 5))
          tiles-with-mines (filter (fn [[k v]] (= (:type v) :mine)) board)]
      (is (= 9 (count board)))
      (is (= 5 (count tiles-with-mines))))))

(deftest count-adjacent-mines-test
  (testing "Mine placed at 3,3"
    (let [board (->> (game/empty-board 3)
                     (place-mine-at 3 3))]
      (is (= 1 (game/count-adjacent-mines 3 4 board)))
      (is (= 0 (game/count-adjacent-mines 3 3 board)))
      (is (= 0 (game/count-adjacent-mines 0 0 board))))))

(deftest one-adjacent-mine
  (testing "Tile type is :1 when there is 1 adjacent mine"
    (let [board (->> (game/empty-board 2)
                     (place-mine-at 0 0)
                     (game/label-tiles-with-adjacent-mines))
          tile-type (fn [k] (:type (get board k)))]
      (is (= :mine (tile-type "0,0")))
      (is (= :1 (tile-type "0,1")))
      (is (= :1 (tile-type "1,0")))
      (is (= :1 (tile-type "1,1")))
      (is (= 4 (count board))))))

(deftest two-adjacent-mines
  (let [board (->> (game/empty-board 3)
                   (place-mine-at 0 0)
                   (place-mine-at 1 0)
                   (game/label-tiles-with-adjacent-mines))
        tile-type (fn [k] (:type (get board k)))]

    (testing "top row"
      (is (= :mine (tile-type "0,0")))
      (is (= :mine (tile-type "1,0")))
      (is (= :1 (tile-type "2,0"))))

    (testing "middle row"
      (is (= :2 (tile-type "0,1")))
      (is (= :2 (tile-type "1,1")))
      (is (= :1 (tile-type "2,1"))))

    (testing "bottom row"
      (is (= :0 (tile-type "0,2")))
      (is (= :0 (tile-type "1,2")))
      (is (= :0 (tile-type "2,2"))))

    (is (= 9 (count board)))))

(deftest initialise-board
  (let [size 9
        num-mines 20
        board (game/init-board size num-mines)
        tiles-with-mines (filter (fn [[k v]] (= (:type v) :mine)) board)]
    (is (= 81 (count board)))
    (is (= 20 (count tiles-with-mines)))))

(defcard board-with-single-mine-reveal-empty-space
  (let [board (->> (game/empty-board 9)
                   (place-mine-at 0 0)
                   (game/label-tiles-with-adjacent-mines))
        board-revealed (game/reveal-adjacent-empty-tiles board "8,8")
        assert-revealed (fn [k] (is (= :revealed (:state (get board-revealed k)))))]
    (reagent/as-element
     [:div
      [:p "Before: (revealed board and actual board)"]
      (view/render-board (game/reveal-all board) clicking-enabled)
      [:span " "]
      (view/render-board board clicking-enabled)
      [:p "After clicking on the bottom right tile:"]

      (view/render-board board-revealed clicking-enabled)

      (assert-revealed "1,3")
      (assert-revealed "3,0")])))

(deftest board-with-single-mine-reveal-empty-space-test
  (testing "Clicking on an empty square reveals all adjacent empty squares"
    (let [board (->> (game/empty-board 3)
                     (place-mine-at 0 0)
                     (game/label-tiles-with-adjacent-mines))
          board-after (game/reveal-adjacent-empty-tiles board "2,2")
          tile-state (fn [k] (:state (get board-after k)))]

      (testing "Mine at 0,0 is unrevealed"
        (is (= :unrevealed (tile-state "0,0"))))

      (testing "All other tiles revealed"
        (let [revealed-keys (select-tile-keys board-after :revealed)]
          (is (= ["0,1" "0,2" "1,0" "1,1" "1,2" "2,0" "2,1" "2,2"] revealed-keys)))))))

(defcard board-with-two-mines-reveal-empty-space
  (let [board (->> (game/empty-board 5)
                   (place-mine-at 0 0)
                   (place-mine-at 3 4)
                   (game/label-tiles-with-adjacent-mines))
        board-after (game/reveal-adjacent-empty-tiles board "4,0")]
    (reagent/as-element
     [:div
      [:p "Before: (revealed board and actual board)"]
      (view/render-board (game/reveal-all board) clicking-enabled)
      [:span " "]
      (view/render-board board clicking-enabled)
      [:p "After clicking the last tile on the right on the first row:"]
      (view/render-board board-after clicking-enabled)])))

(deftest board-with-two-mines-reveal-empty-space-test
  (testing "Clicking on an empty square reveals all adjacent empty squares"
    (let [board (->> (game/empty-board 5)
                     (place-mine-at 0 0)
                     (place-mine-at 3 4)
                     (game/label-tiles-with-adjacent-mines))
          board-after (game/reveal-adjacent-empty-tiles board "4,0")
          tile-state (fn [k] (:state (get board-after k)))
          revealed-keys (select-tile-keys board-after :revealed)]

      (testing "The two mines are unrevealed"
        (is (= :unrevealed (tile-state "0,0")))
        (is (= :unrevealed (tile-state "3,4"))))

      (testing "The bottom right tile remains unrevealed"
        (is (= :unrevealed (tile-state "4,4"))))

      (testing "All other tiles are revealed"
        (is (= ["0,1" "0,2" "0,3" "0,4" "1,0" "1,1" "1,2" "1,3" "1,4"
                "2,0" "2,1" "2,2" "2,3" "2,4" "3,0" "3,1" "3,2" "3,3"
                "4,0" "4,1" "4,2" "4,3"]
               revealed-keys))))))

(deftest reveal-adjacent-tiles-only-reveals-single-tile
  (testing "Clicking on a tile with one surrounding mine reveals only that tile"
    (let [board (->> (game/empty-board 3)
                     (place-mine-at 0 0)
                     (game/label-tiles-with-adjacent-mines))
          board-after (game/reveal-adjacent-empty-tiles board "1,1")
          revealed-tiles (select-tile-keys board-after :revealed)
          unrevealed-keys (select-tile-keys board-after :unrevealed)]

      (testing "Tile at 1,1 is revealed"
        (is (= ["1,1"] revealed-tiles)))

      (testing "All other tiles are unrevealed"
        (is (= ["0,0" "0,1" "0,2" "1,0" "1,2" "2,0" "2,1" "2,2"] unrevealed-keys))))))

(deftest reveal-all-mines
  (let [board (->> (game/empty-board 3)
                   (place-mine-at 0 0)
                   (place-mine-at 1 1)
                   (place-mine-at 2 2))
        board-after (game/reveal-all-mines board)
        revealed-tiles (select-tile-keys board-after :revealed)
        unrevealed-tiles (select-tile-keys board-after :unrevealed)]
    (testing "mines are revealed"
      (is (= ["0,0" "1,1" "2,2"] revealed-tiles)))
    (testing "other tiles remain unrevealed"
      (is (= ["0,1" "0,2" "1,0" "1,2" "2,0" "2,1"] unrevealed-tiles)))))

(deftest all-non-mine-tiles-revealed-test
  (testing "2x2 grid with mine at 0,0"
    (let [game-state (atom {:board (->> (game/empty-board 2)
                                        (place-mine-at 0 0))
                            :game-state :in-progress})]
      (testing "0,1 revealed - game not won"
        (game/reveal-tile! game-state "0,1")
        (is (false? (game/all-non-mine-tiles-revealed? game-state))))

      (testing "1,0 revealed - game not won"
        (game/reveal-tile! game-state "1,0")
        (is (false? (game/all-non-mine-tiles-revealed? game-state))))

      (testing "1,1 revealed - only the mine left unrevealed"
        (game/reveal-tile! game-state "1,1")
        (is (true? (game/all-non-mine-tiles-revealed? game-state)))

        (testing "Game won!"
          (is (= :won (-> game-state deref :game-state))))))))
