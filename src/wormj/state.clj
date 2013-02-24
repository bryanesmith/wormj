(ns wormj.state
  (:require [wormj.functions :as f]))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(def init-worm-len 8)         ; Initial worm length is 8
(def worm       (ref nil))
(def board      (ref nil))
(def trajectory (ref :right)) ; :right :left :up :down

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn set-trajectory
  "Set trajectory"
  [way]
  {:pre [(f/valid-direction? way)]}
    (dosync (ref-set trajectory way)))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn gen-apple
  "Generates a random apple."
  [max-x max-y worm]
  (loop []
    (let [val (int (+ 1 (rand-int 9)))
          new-x (rand-int max-x)
          new-y (rand-int max-y)
          apple (f/build-apple val new-x new-y)]
      (if-not (some #(= (:position apple) %) (:position worm))
        apple
        (recur)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn advance-turn
  "Advance one turn: using trajectory, move worm, potentially consume/generate apple."
  []
  (let [moved-worm (f/move-worm @worm @trajectory)]
    (if (f/apple-consumed? @board moved-worm)
      ; Apple consumed - modify, then set, new worm to have growth count & generate/set new apple
      (let [g-c (get-in @board [:apple :val])
            b-x (get-in @board [:size :x])
            b-y (get-in @board [:size :y])
            grow-worm (assoc moved-worm :grow-count g-c)]
        (dosync
          (ref-set worm grow-worm)
          (ref-set board (assoc @board :apple (gen-apple b-x b-y grow-worm)))))
      ; Apple not consumed - set moved worm
      (dosync(ref-set worm moved-worm)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn init-game
  "Initialize the game."
  [b-width b-height]
  (set-trajectory :right)
  (let [worm# (f/gen-worm 0 (quot b-height 2) init-worm-len)]
    (dosync
      (ref-set worm worm#)
      (ref-set board (f/gen-board (gen-apple b-width b-height worm#) b-width b-height)))))

