(ns minesweeper.app-test
  (:require [cljs.test :refer-macros [is testing async]]
            [devcards.core :refer-macros [deftest]]
            [minesweeper.app :refer [adder]]))

(deftest a-test
  (testing "Adder"
    (is (= 5 (adder 1 4)))))

(deftest async-test
  (testing "some async test..."
    (async done
           (js/setTimeout (fn []
                            (is (= 1 1))
                            (done))
                          50))))
