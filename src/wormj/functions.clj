(ns wormj.functions)

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn gen-board
  "Generates a board"
  [width height]
  {:x width :y height})

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn gen-worm-position
  "Creates coordinates for a worm."
  [x, y, len]
  (if (< len 1)
    (throw (Exception. "Length must be 1 or greater."))
    (vec (for [x# (range x (+ x len))]
      {:x x# :y y}))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn build-worm
  "Builds a worm"
  [init-len, grow-count, pos]
  { :initial-len init-len
    :grow-count  grow-count
    :position    pos        })

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn gen-worm
  "Generate a worm of specific length, starting at specified coordinates"
  [x, y, len]
  (build-worm len 0 (gen-worm-position x y len)))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn collide-wall?
  "Returns true if worm collides with wall"
  [worm board]
  (let [w_x (:x (last (:position worm)))
        w_y (:y (last (:position worm)))
        b_x (:x board)
        b_y (:y board)]
    (or (= w_x -1)
        (= w_y -1)
        (= w_x b_x)
        (= w_y b_y))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn collide-self?
  "Returns true if worm hits self"
  [worm]
  (let [ pos  (:position worm)
         head (peek pos)
         body (pop pos) ]
    (true? (some #(= head %) body)))) ; Use true to evaluate nil as false

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn game-over?
  "Returns true if game is over"
  [worm board]
  (or (collide-wall? worm board)
      (collide-self? worm)))

