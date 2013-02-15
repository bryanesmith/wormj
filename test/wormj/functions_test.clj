(ns wormj.functions-test
  (:use clojure.test
    wormj.functions))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(deftest test-gen-worm-position
  (is (thrown? Exception (gen-worm 0 0 0)))
  (is (= (gen-worm-position 0 0 1) [{:x 0 :y 0}]))
  (is (= (gen-worm-position 0 0 3) [{:x 0 :y 0} {:x 1 :y 0} {:x 2 :y 0}]))
  (is (= (gen-worm-position 2 0 3) [{:x 2 :y 0} {:x 3 :y 0} {:x 4 :y 0}]))
  (is (= (gen-worm-position 0 2 3) [{:x 0 :y 2} {:x 1 :y 2} {:x 2 :y 2}]))
  (is (= (gen-worm-position 3 4 3) [{:x 3 :y 4} {:x 4 :y 4} {:x 5 :y 4}])))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(deftest test-gen-worm
  (let [small (gen-worm 0 0 1)
        med   (gen-worm 0 13 4)
        large (gen-worm 2 3 8)]
    ; small
    (is (= (:initial-len small) 1))
    (is (= (:grow-count  small) 0))
    (is (= (:position    small) [{:x 0 :y 0}]))
    
    ; med
    (is (= (:initial-len med) 4))
    (is (= (:grow-count  med) 0))
    (is (= (:position    med) [{:x 0 :y 13} {:x 1 :y 13} {:x 2 :y 13} {:x 3 :y 13}]))

    ; large
    (is (= (:initial-len large) 8))
    (is (= (:grow-count  large) 0))
    (is (= (:position    large) [{:x 2 :y 3} {:x 3 :y 3} {:x 4 :y 3} {:x 5 :y 3} {:x 6 :y 3} {:x 7 :y 3} {:x 8 :y 3} {:x 9 :y 3}]))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(def tail-collide (build-worm 3 0 [{:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 5}]))
(def body-collide (collide-self? (build-worm 3 0 [{:x 4 :y 5} {:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 5}])))
(def  nil-collide (build-worm 3 0 [{:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 7}]))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(def worm-1  (gen-worm 5  8 4))     ; Head => {:x 8 :y 8}
(def worm-2  (gen-worm 1 -1 4))     ; Collides y
(def board-1 (gen-board 8 9))       ; worm-1 => collides x
(def board-2 (gen-board 9 8))       ; worm-1 => collides y
(def board-3 (gen-board 9 9))       ; worm-1 => no collide

(deftest test-collide-wall?
  (is (true?  (collide-wall? worm-1 board-1)))
  (is (true?  (game-over? worm-1 board-1)))

  (is (true?  (collide-wall? worm-1 board-2)))
  (is (true?  (game-over? worm-1 board-2)))

  (is (false? (collide-wall? worm-1 board-3)))
  (is (false? (game-over? worm-1 board-3)))

  (is (true?  (collide-wall? worm-2 board-1)))
  (is (true?  (game-over? worm-2 board-1))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(def tail-collide (build-worm 3 0 [{:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 5}]))
(def body-collide (build-worm 3 0 [{:x 4 :y 5} {:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 5}]))
(def  nil-collide (build-worm 3 0 [{:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 7}]))

(deftest test-collide-self?
  (is (true?  (collide-self? tail-collide)))   ; Collides with tail
  (is (true?  (game-over? tail-collide board-1)))   

  (is (true?  (collide-self? body-collide)))   ; Collides with body
  (is (true?  (game-over? body-collide board-1)))  

  (is (false? (collide-self? nil-collide)))    ; No collision
  (is (false? (game-over? nil-collide board-1)))) 

