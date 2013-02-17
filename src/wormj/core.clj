(ns wormj.core
  (:require [wormj.functions   :as f]
            [wormj.state       :as s]
            [lanterna.terminal :as t])
  (:gen-class))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~  
(defn r-map-key
  "Given a point, creates key."
  [point]
  (keyword (str (:x point) "_" (:y point))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn r-map
  "Returns a map of maps with character data. (x_y (render-game-map))"
  [worm board]
  (let [ apple     (:apple board)
         apple-key (r-map-key (:position apple)) 
         apple-val (:val apple)
         pos       (:position worm)
         head      (peek pos)
         head-key  (r-map-key head)
         body      (pop pos)]
  (into {apple-key (char (+ 48 apple-val)) head-key \@ } 
        (map #(vector (r-map-key %) \o) body))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn render-wall
  "Renders top or bottom wall to string"
  [type width]
  (case type
    :top    (str "┌" (apply str (take width (repeat "*"))) "┐")
    :bottom (str "└" (apply str (take width (repeat "*"))) "┘")
    (throw (Exception. (str "Invalid wall type: " type)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn render-row
  "Renders a row on the board to string"
  [y width rmap]  
  (apply str
    (for [x (range 0 width)]
      (let [key (r-map-key {:x x :y y})]
        (key rmap " ")))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn render-game
  "Render current game state as collection of strings"
  []
  (let [board  @wormj.state/board
        worm   @wormj.state/worm
        width  (:x (:size board))
        height (:y (:size board))
        rmap   (r-map worm board)]
    (flatten
      (vector
        (render-wall :top width)  
        (for [y (range 0 height)]
          (str "*" (render-row y width rmap) "*"))
        (render-wall :bottom width)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn print-game-over-msg
  ""
  []
  (println)
  (println "Well, you ran into something and the game is over.")
  (println "Your final score was" (f/score @s/worm))
  (println))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
; TODO: the type of terminal should be specified by user.
(def term (t/get-terminal :unix))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn draw-board
  "Draw the board to the terminal"
  []
  (let [board-width  (:x (:size @s/board))
        board-height (:y (:size @s/board))]

    ; "Worm"
    (t/move-cursor term 1 0)
    (t/put-string term "Wormj")

    ; Top wall
    (t/move-cursor term 0 1)
    (t/put-string term (render-wall :top board-width))

    ; Side walls
    (loop [i 0]
      (when (< i board-height)
        (t/move-cursor term 0 (+ 2 i))
        (t/put-string term "*")
        (t/move-cursor term (+ board-width 1) (+ 2 i) )
        (t/put-string term "*")
        (recur (inc i))))

    ; Bottom wall
    (t/move-cursor term 0 (+ board-height 2))
    (t/put-string term (render-wall :bottom board-width))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn draw-updates
  "Updates screen: worm, apple, score"
  []
  (let [b-width  (:x (:size @s/board))
        b-height (:y (:size @s/board))
        rmap   (r-map @s/worm @s/board)]
    (doseq [x (range 0 b-width) 
            y (range 0 b-height)]
      (let [rkey (r-map-key {:x x :y y})]
        (t/move-cursor term (+ x 1) (+ y 2))
        (t/put-character term (rkey rmap \space ))))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn run
  "Handles execution game once terminal is stated"
  []
  (let [term-size (t/get-size term)
        board-x   (- (first term-size) 2)
        board-y   (- (last term-size)  3)]
    (wormj.state/init-game board-x board-y)
    (draw-board)
    (loop [] ; Loop until game over
      (if-not (f/game-over? @s/worm @s/board)
        (do
          (draw-updates)         
          (. Thread (sleep 50))
          (s/advance-turn)
          (recur))))))
; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn -main
  "User runs game."
  [& args]
  (try 
    (do
      (t/start term)
      (run))
  (catch Exception e
    (println "An error occured: " (.getMessage e)))
  (finally 
    (do
      (t/stop term)
      (print-game-over-msg)))))

