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
(deftest test-collide-wall?
  (let [worm-1  (gen-worm 5  8 4)   ; Head => {:x 8 :y 8}
        worm-2  (gen-worm 1 -1 4)   ; Collides y
        board-1 (gen-board 8 9)     ; worm-1 => collides x
        board-2 (gen-board 9 8)     ; worm-1 => collides y
        board-3 (gen-board 9 9)]    ; worm-1 => no collide

    (is (true?  (collide-wall? worm-1 board-1)))
    (is (true?  (game-over? worm-1 board-1)))

    (is (true?  (collide-wall? worm-1 board-2)))
    (is (true?  (game-over? worm-1 board-2)))

    (is (false? (collide-wall? worm-1 board-3)))
    (is (false? (game-over? worm-1 board-3)))

    (is (true?  (collide-wall? worm-2 board-1)))
    (is (true?  (game-over? worm-2 board-1)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(def tail-collide (build-worm 3 0 [{:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 5}]))
(def body-collide (build-worm 3 0 [{:x 4 :y 5} {:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 5}]))
(def  nil-collide (build-worm 3 0 [{:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 7}]))

(deftest test-collide-self?
  (let [tail-collide (build-worm 3 0 [{:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 5}])
        body-collide (build-worm 3 0 [{:x 4 :y 5} {:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 5}])
        nil-collide (build-worm 3 0 [{:x 5 :y 5} {:x 6 :y 5} {:x 6 :y 6} {:x 5 :y 6} {:x 5 :y 7}])
        board-1 (gen-board 8 9)] 

    (is (true?  (collide-self? tail-collide)))   ; Collides with tail
    (is (true?  (game-over? tail-collide board-1)))   

    (is (true?  (collide-self? body-collide)))   ; Collides with body
    (is (true?  (game-over? body-collide board-1)))  

    (is (false? (collide-self? nil-collide)))    ; No collision
    (is (false? (game-over? nil-collide board-1)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(deftest test-update-head
  (is (= (update-head {:x 1 :y 1} :left  ) {:x 0 :y 1}))
  (is (= (update-head {:x 1 :y 1} :right ) {:x 2 :y 1}))
  (is (= (update-head {:x 1 :y 1} :up    ) {:x 1 :y 0}))
  (is (= (update-head {:x 1 :y 1} :down  ) {:x 1 :y 2}))
  (is (= (update-head {:x 0 :y 0} :left  ) {:x -1 :y 0}))
  (is (= (update-head {:x 0 :y 0} :up    ) {:x 0 :y -1})))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(deftest test-move-worm

  (let [worm-1 (build-worm 1 2 [{:x 2 :y 2}])
        worm-2 (build-worm 1 1 [{:x 2 :y 2} {:x 2 :y 1}])
        worm-3 (build-worm 1 0 [{:x 2 :y 2} {:x 2 :y 1} {:x 3 :y 1}])
        worm-4 (build-worm 1 0 [{:x 2 :y 1} {:x 3 :y 1} {:x 3 :y 2}])
        worm-5 (build-worm 1 0 [{:x 3 :y 1} {:x 3 :y 2} {:x 2 :y 2}])]
  
  ; Worm goes up, grows, growth counter 2 -> 1
  (is (= (move-worm worm-1 :up) worm-2))

  ; Worm goes right, grows, growth counter 1-> 0
  (is (= (move-worm worm-2 :right) worm-3))
 
  ; Worm goes down, no growth, growth counter remains at 0
  (is (= (move-worm worm-3 :down) worm-4))

  ; Worm goes left, no growth, growth counter remains at 0
  (is (= (move-worm worm-4 :left) worm-5))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(deftest test-apple?
  (is (true?  (apple? (build-board (build-apple 5 1 2) {:x 3 :y 4}))))
  (is (false? (apple? (build-board nil                 {:x 3 :y 4}))))) 

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(deftest test-apple-consumed?
  (let [worm (build-worm 1 0 [{:x 3 :y 1} {:x 3 :y 2} {:x 2 :y 2}])
        board-1 (build-board nil {:x 10 :y 10})
        board-2 (build-board (build-apple 3 3 3) {:x 10 :y 10})
        board-3 (build-board (build-apple 3 2 2) {:x 10 :y 10})]
    (is (false? (apple-consumed? board-1 worm)))
    (is (false? (apple-consumed? board-2 worm)))
    (is (true?  (apple-consumed? board-3 worm)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(deftest test-score
  (is (= 0 (score (build-worm 3 0 [{:x 0 :y 0} {:x 1 :y 0} {:x 2 :y 0}]))))
  (is (= 3 (score (build-worm 3 3 [{:x 0 :y 0} {:x 1 :y 0} {:x 2 :y 0}]))))                 ; growth counter of 3
  (is (= 1 (score (build-worm 3 0 [{:x 0 :y 0} {:x 1 :y 0} {:x 2 :y 0} {:x 3 :y 0}]))))     ; grew 1
  (is (= 4 (score (build-worm 3 3 [{:x 0 :y 0} {:x 1 :y 0} {:x 2 :y 0} {:x 3 :y 0}])))))    ; growth counter of 3 and grew 1


