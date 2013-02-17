(ns wormj.core
  (:require [wormj.functions   :as f]
            [wormj.state       :as s]
            [lanterna.terminal :as t])
  (:gen-class))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~  
(defn to-str-map-key
  "Given a point, creates key."
  [point]
  (keyword (str (:x point) "_" (:y point))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn to-str-map
  "Returns a map of maps with character data. (x_y (game-to-stto-str-map))"
  [worm board]
  (let [ apple     (:apple board)
         apple-key (to-str-map-key (:position apple)) 
         apple-val (:val apple)
         pos       (:position worm)
         head      (peek pos)
         head-key  (to-str-map-key head)
         body      (pop pos)]
  (into {apple-key (char (+ 48 apple-val)) head-key \@ } 
        (map #(vector (to-str-map-key %) \o) body))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn wall-to-str
  "Renders top or bottom wall to string"
  [type width]
  (case type
    :top    (str "┌" (apply str (take width (repeat "*"))) "┐")
    :bottom (str "└" (apply str (take width (repeat "*"))) "┘")
    (throw (Exception. (str "Invalid wall type: " type)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn row-to-str
  "Renders a row of the board  to string"
  [y width str-map]  
  (apply str
    (for [x (range 0 width)]
      (let [key (to-str-map-key {:x x :y y})]
        (key str-map " ")))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn game-to-str
  "Render current game state as collection of strings"
  []
  (let [board  @wormj.state/board
        worm   @wormj.state/worm
        width  (:x (:size board))
        height (:y (:size board))
        str-map   (to-str-map worm board)]
    (flatten
      (vector
        (wall-to-str :top width)  
        (for [y (range 0 height)]
          (str "*" (row-to-str y width str-map) "*"))
        (wall-to-str :bottom width)))))

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
    (t/put-string term (wall-to-str :top board-width))

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
    (t/put-string term (wall-to-str :bottom board-width))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
; Source: http://goo.gl/Teu2N 
; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn subtract-lists[a b]
   (remove (into #{} b) a))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(def prev-apple (ref nil))
(def prev-worm  (ref nil))
(def board-char (char \space))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn draw-board-character
  "Draw character at specified board (not screen) coord."
  [c coord]
  (t/move-cursor term (+ 1 (:x coord)) (+ 2 (:y coord)))
  (t/put-character term c))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn draw-updates
  "Updates screen: worm, apple, score"
  []

  ; If prev-worm set, overwrite any positions that
  ; are no longer part of worm.
  (if-not (nil? @prev-worm)
    (let [old-w (:position @prev-worm)
          new-w (:position @s/worm)]
    (doseq [seg (subtract-lists old-w new-w)]
      (draw-board-character board-char seg))))

  ; Render worm body
  (if (nil? @prev-worm)

    ; No prev-worm, so draw entire worm
    (doseq [seg (pop (:position @s/worm))]
      (draw-board-character \o seg))

    ; Prev-worm, just draw difference
    (let [old-w (pop (:position @prev-worm))
          new-w (pop (:position @s/worm))]
      (doseq [seg (subtract-lists new-w old-w)]
        (draw-board-character \o seg))))

  ; Render worm head
  (draw-board-character \@ (peek (:position @s/worm)))

  ; Render apple (if necessary)
  (if (or (nil? @prev-apple)
          (not= @prev-apple (:apple @s/board)))
    (let [apple (:apple @s/board)
          val   (:val   apple)
          pos   (:position apple)]
      (draw-board-character (char (+ 48 val)) pos)))

  ; Move cursor out of way
  (t/move-cursor term 999 999)
  
  ; Store ref to worm
  (dosync
    (ref-set prev-worm  @s/worm)
    (ref-set prev-apple (:apple @s/board))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn run-in-term
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
          (. Thread (sleep 100))
          (s/advance-turn)
          (recur))))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn -main
  "User runs game."
  [& args]
  (try 
    (do
      (t/start term)
      (run-in-term))
  (catch Exception e
    (println "An error occured: " (.getMessage e)))
  (finally 
    (do
      (t/stop term)
      (print-game-over-msg)))))

