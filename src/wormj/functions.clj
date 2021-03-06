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
  ; note: for x & y, -1 is a legal positon
  ;       (although the game is over) 
  {:pre [(>= x -1) (>= y -1) (>= len 1)]}
  (vec (for [x# (range x (+ x len))]
    {:x x# :y y})))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn gen-worm
  "Generate an initial worm of specific length, starting at specified coordinates"
  [x, y, len]
  (build-worm len 0 (gen-worm-position x y len)))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn collide-wall?
  "Returns true if worm collides with wall"
  [worm board]
  (let [head (peek (:position worm))
        w_x  (:x head)
        w_y  (:y head)
        b_x  (:x (:size board))
        b_y  (:y (:size board))]

    ; Assert that worm has not left the stage
    (assert (>= w_x -1))
    (assert (>= w_y -1))
    (assert (<= w_x b_x))
    (assert (<= w_y b_y))

    ; Run into wall
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

    ; Use true to evaluate nil as false
    (true? (some #(= head %) body)))) 

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn game-over?
  "Returns true if game is over"
  [worm board]
  (or (collide-wall? worm board)
      (collide-self? worm)))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn valid-direction? [way]
  (if (some #(= way %) [:right :left :up :down])
    true
    false))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn update-head
  "Given previous head and direction, returns new head. Doesn't check whether valid." 
  [prev-head direction]
  {:pre [(not (nil? prev-head)) (valid-direction? direction)]}
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
    ; Eaten iff position(head) = position(apple)
    (= (:position (:apple board)) (peek (:position worm))) 
    false))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn update-worm-pos
  "Generates new worm from old worm and direction."
  [worm direction]
  (let [prev-pos  (:position worm)
        prev-head (peek prev-pos)
        up-head   (update-head prev-head direction)]
        
    (if (growing? worm)

      ; Still growing, don't drop tail
      (conj prev-pos up-head)

      ; Not growing, drop tail
      (conj (vec (rest prev-pos)) up-head))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn move-worm
  "Given specified direction, returns new worm"
  [worm direction]
  {:pre [(not (nil? worm)) (wormj.functions/valid-direction? direction)]}
  (let [grow-rem  (max 0 (dec (:grow-count worm)))
        next-pos  (update-worm-pos worm direction)
        init-len  (:initial-len worm)]
    (build-worm init-len grow-rem next-pos))) 

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn score
  "Calculates current score"
  [worm]
  {:pre [(not (nil? worm))]}
  ; score = length + grow-count - initial-len
  (let [length   (count (:position worm))
        grow-cnt (:grow-count worm)
        init-len (:initial-len worm)]
    (-> length
      (+ grow-cnt)
      (- init-len))))     

