(ns wormj.core
  (:require wormj.functions
            wormj.state))

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
  (into {apple-key apple-val head-key "@" } 
        (map #(vector (r-map-key %) "o") body))))

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

