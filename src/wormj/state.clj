(ns wormj.state
  (:require wormj.functions))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(def init-worm-len 8)         ; Initial worm length is 8
(def worm       (ref nil))
(def board      (ref nil))
(def trajectory (ref :right)) ; :right :left :up :down

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn set-trajectory
  "Set trajectory"
  [way]
  (if-not (some #(= way %) [:right :left :up :down])
    (throw (Exception. (str "Invalid direction: " way)))
    (dosync (ref-set trajectory way))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn gen-apple
  "Generates a random apple. (No side effects; the reason this is not in functional layer is because random output.)"
  [max-x max-y worm]
  (loop []
    (let [val (int (+ 1 (rand-int 9)))
          new-x (rand-int max-x)
          new-y (rand-int max-y)
          apple (wormj.functions/build-apple val new-x new-y)]
      (if-not (some #(= (:position apple) %) (:position worm))
        apple
        (recur)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn advance-turn
  "Advance one turn: using trajectory, move worm, potentially consume/generate apple."
  []
  (let [moved-worm (wormj.functions/move-worm @worm @trajectory)]
    (dosync(ref-set worm moved-worm))
    (if (wormj.functions/apple-consumed? @board @worm)
      ; Apple consumed - modify, then set, new worm to have growth count & generate/set new apple
      (let [growing-worm (assoc moved-worm :grow-count (:val (:apple @board)))]
        (dosync
          (ref-set worm growing-worm)
          (ref-set board (assoc @board :apple (gen-apple (:x (:size @board)) (:y (:size @board)) growing-worm))))))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn init-game
  "Initialize the game."
  [b-width b-height]
  (set-trajectory :right)
  (let [worm# (wormj.functions/gen-worm 0 (quot b-height 2) init-worm-len)]
    (dosync
      (ref-set worm worm#)
      (ref-set board (wormj.functions/gen-board (gen-apple b-width b-height worm#) b-width b-height)))))

