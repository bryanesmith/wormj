(ns wormj.functions)

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn build-board
  "Build a board"
  [apple size]
  {:apple apple
   :size  size})

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn build-apple
  "Build an apple"
  [val x y]
  {:val val :position {:x x :y y}})

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn build-worm
  "Builds a worm"
  [init-len, grow-count, pos]
  { :initial-len init-len
    :grow-count  grow-count
    :position    pos        })

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn gen-board
  "Generates an initial board"
  [apple width height]
  (build-board apple {:x width :y height}))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn gen-worm-position
  "Creates coordinates for a worm."
  [x, y, len]
  (if (< len 1)
    (throw (Exception. "Length must be 1 or greater."))
    (vec (for [x# (range x (+ x len))]
      {:x x# :y y}))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn gen-worm
  "Generate an initial worm of specific length, starting at specified coordinates"
  [x, y, len]
  (build-worm len 0 (gen-worm-position x y len)))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn collide-wall?
  "Returns true if worm collides with wall"
  [worm board]
  (let [w_x (:x (last (:position worm)))
        w_y (:y (last (:position worm)))
        b_x (:x (:size board))
        b_y (:y (:size board))]
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

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn update-head
  "Given previous head and direction, returns new head. Doesn't check whether valid." 
  [prev-head direction]
  (let [x (:x prev-head)
        y (:y prev-head)]
    (case direction
      :left  { :x (- x 1) :y y }
      :right { :x (+ x 1) :y y }
      :up    { :x x :y (- y 1) }
      :down  { :x x :y (+ y 1) })))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn growing?
  "Returns true if worm is growing"
  [worm]
  (> (:grow-count worm) 0))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn apple?
  "Returns true if an apple has been set on the board."
  [board]
  (not (nil? (:apple board))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn apple-consumed?
  "Returns true if apple consumed."
  [board worm]
  (if (apple? board)
    (= (:position (:apple board)) (peek (:position worm))) ; Eaten iff position(head) = position(apple)
    false))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn move-worm
  "Given specified direction, returns new worm"
  [worm direction]
  (let [pos       (:position worm)
        prev-head (peek pos)
        next-head (update-head prev-head direction)]
    (build-worm (:initial-len worm) 
                (max 0 (dec (:grow-count worm)))          ; Decrement counter
                (if (growing? worm)
                  (conj pos next-head)                    ; Still growing, don't drop tail
                  (conj (vec (rest pos)) next-head)))))   ; Not growing, drop tail

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn score
  "Calculates current score"
  [worm]
  (- (+ (count (:position worm)) (:grow-count worm))     ; score = length + grow-count - initial-len
     (:initial-len worm)))

