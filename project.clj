(defproject minesweeper "0.1.0-SNAPSHOT"
  :description "Implementation of the classic game Minesweeper in ClojureScript"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.227"]
                 [reagent "0.6.0"]]
  :plugins [[lein-cljsbuild "1.1.4" :exclusions [[org.clojure/clojure]]]
            [lein-figwheel "0.5.8"]
            [lein-doo "0.1.7"]]
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"] ;; for emacs cider jack in cljs
                                  [figwheel-sidecar "0.5.8"] ;; to access figwheel from clojure repl
                                  [lein-doo "0.1.7"]
                                  [devcards "0.2.1-7"]]
                   :source-paths ["src" "dev"]}}
  :clean-targets ^{:protect false} [:target-path "out" "resources/public/cljs"]
  :figwheel { :css-dirs ["resources/public/css"]}
  :cljsbuild {;;:test-commands {"testy" ["lein" "doo" "phantom" "test" "once"]}
              :builds [{:id "dev"
                        :source-paths ["src" "dev"]
                        :figwheel true
                        :compiler {:main minesweeper.core
                                   :asset-path "cljs/dev/out"
                                   :output-dir "resources/public/cljs/dev/out"
                                   :output-to "resources/public/cljs/dev/main.js"
                                   :source-map-timestamp true}}
                       {:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:main runners.doo
                                   :optimizations :none
                                   :output-dir "resources/public/cljs/test/out"
                                   :output-to "resources/public/cljs/test/all-tests.js"}}
                       {:id "devcards-test"
                        :source-paths ["src" "test"]
                        :figwheel {:devcards true}
                        :compiler {:main runners.browser
                                   :optimizations :none
                                   :asset-path "cljs/devcards-test/out"
                                   :output-dir "resources/public/cljs/devcards-test/out"
                                   :output-to "resources/public/cljs/devcards-test/devcards.js"
                                   :source-map-timestamp true}}
                       {:id "min"
                        :source-paths ["src"]
                        :compiler {:main minesweeper.core
                                   :output-to "resources/public/cljs/min/main.min.js"
                                   :output-dir "resources/public/cljs/min/out"
                                   :optimizations :advanced
                                   :pretty-print false}}]}
  :aliases {"test-cljs" ["doo" "phantom" "test" "once"]
            "build-cljs" ["do" "clean" ["cljsbuild" "once" "min"]]})
