(ns wormj.state-test
  (:use clojure.test
    wormj.functions
    wormj.state))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(deftest test-gen-apple
  (loop [i 0]

    ; - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    ; 4 x 4 board is 16 spaces.
    ;
    ; 2 spaces occupied by worm / 16 total spaces = 1/8 chance
    ;   apple generated on segment of worm and subsequently
    ;   regenerated.
    ;
    ; 100 repetitions corresponds to 12.5 expected regenerations.
    ;
    ; Make it incredibly likely that this condition is tested. =)
    ; - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -    
    (when (< i 100)
      (let [max       4
            apple     (gen-apple 4 4 (build-worm 2 0 [{:x 0 :y 2} {:x 1 :y 2}]))
            apple-val (:val apple)
            apple-x   (:x   (:position apple))
            apple-y   (:y   (:position apple))]

        ; Verify correct types
        (is (= java.lang.Integer (type apple-val)))
        (is (= java.lang.Integer (type apple-x)))
        (is (= java.lang.Integer (type apple-y)))

        ; Verify correct ranges
        (is (some #(= apple-val %) (range 0 10)))
        (is (some #(= apple-x %)   (range 0 max)))
        (is (some #(= apple-y %)   (range 0 max)))

        ; Verify not appearing within any segment of worm
        (is (not-any? #(= (:position apple) %) (:position worm))))

      (recur (inc i)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(deftest test-init-game
  (init-game 10 10)
  (is (not (nil? @wormj.state/worm)))
  (is (= wormj.state/init-worm-len (:initial-len @wormj.state/worm)))
  (is (= 0 (:grow-count @wormj.state/worm)))
  (is (not (nil? (:position @wormj.state/worm))))
  (is (not (nil? @wormj.state/board)))
  (is (not (nil? (:apple @wormj.state/board))))
  (is (not (nil? (:size  @wormj.state/board)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(deftest test-set-trajectory
  (set-trajectory :right)
  (is (= :right @trajectory))
  (set-trajectory :left)
  (is (= :left @trajectory))
  (set-trajectory :up)
  (is (= :up @trajectory))
  (set-trajectory :down)
  (is (= :down @trajectory))
  (is (thrown? Exception (set-trajectory :foo))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
; Note: this demonstrates how to use wormj as
;       an API. 
;
;       The API is simple; 1-2 sexprs per turn.
; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(deftest test-advance-turn
 
  ;   ┌************┐
  ; 0 *............*
  ; 1 *............*
  ; 2 *............*
  ; 3 *............*
  ; 4 *............*
  ; 5 *............*
  ; 6 *ooooooo@.2..*
  ; 7 *............*
  ; 8 *............*
  ; 9 *............*
  ; 0 *............*
  ; 1 *............*
  ;   └************┘
  ;    012345678901

  ;//////////////////////////////////
  ;  API
  ;//////////////////////////////////
  (init-game 12 12)

  ;//////////////////////////////////
  ;  Manipulate for testing purposes
  ;//////////////////////////////////
  (is (true? (apple? @board)))                         ; Verify there's an apple before overriding
  (dosync                                              ; Override random apple
    (ref-set board (assoc @board :apple (build-apple 2 9 6)))) 

  ;//////////////////////////////////
  ;  Assertions
  ;//////////////////////////////////
  (is (= {:x 12 :y 12} (:size     @board)))            ; Assert board
  (is (= {:x  9 :y  6} (:position (:apple @board))))   ; Assert apple
  (is (= 2 (:val (:apple @board))))                   
  (is (= {:x  7 :y  6} (peek      (:position @worm)))) ; Assert head
  (is (= {:x  0 :y  6} (first     (:position @worm)))) ; Assert tail
  (is (= 0 (:grow-count  @worm)))
  (is (= :right @trajectory))                          ; Assert trajectory

  ;   :grow-count 0
  ;
  ;   ┌************┐
  ; 0 *............*
  ; 1 *............*
  ; 2 *............*
  ; 3 *............*
  ; 4 *............*
  ; 5 *............*
  ; 6 *.ooooooo@2..*
  ; 7 *............*
  ; 8 *............*
  ; 9 *............*
  ; 0 *............*
  ; 1 *............*
  ;   └************┘
  ;    012345678901

  ;//////////////////////////////////
  ;  API
  ;//////////////////////////////////
  (advance-turn)

  ;//////////////////////////////////
  ;  Assertions
  ;//////////////////////////////////
  (is (= {:x 12 :y 12} (:size     @board)))            ; Assert board
  (is (= {:x  9 :y  6} (:position (:apple @board))))   ; Assert apple
  (is (= 2 (:val (:apple @board))))                   
  (is (= {:x  8 :y  6} (peek      (:position @worm)))) ; Assert head
  (is (= {:x  1 :y  6} (first     (:position @worm)))) ; Assert tail
  (is (= 0 (:grow-count  @worm)))
  (is (= :right @trajectory))                          ; Assert trajectory

  ;   :grow-count 2
  ;
  ;   ┌************┐
  ; 0 *............*
  ; 1 *............*
  ; 2 *............*
  ; 3 *............*
  ; 4 *............*
  ; 5 *............*
  ; 6 *..ooooooo@..*
  ; 7 *............*
  ; 8 *............*
  ; 9 *............*
  ; 0 *..........9.*
  ; 1 *............*
  ;   └************┘
  ;    012345678901

  ;//////////////////////////////////
  ;  API
  ;//////////////////////////////////
  (advance-turn)  

  ;//////////////////////////////////
  ;  Manipulate for testing purposes
  ;//////////////////////////////////
  (is (true? (apple? @board)))                          ; Verify there's an apple before overriding
  (is (not= {:x  9 :y  6} (:position (:apple @board)))) ; Verify new apple
  (dosync                                               ; Override random apple
    (ref-set board (assoc @board :apple (build-apple 9 10 10)))) 

  ;//////////////////////////////////
  ;  Assertions
  ;//////////////////////////////////
  (is (= {:x 12 :y 12} (:size     @board)))            ; Assert board
  (is (= {:x 10 :y 10} (:position (:apple @board))))   ; Assert apple
  (is (= 9 (:val (:apple @board))))                   
  (is (= {:x  9 :y  6} (peek      (:position @worm)))) ; Assert head
  (is (= {:x  2 :y  6} (first     (:position @worm)))) ; Assert tail
  (is (= 2 (:grow-count  @worm)))
  (is (= :right @trajectory))                          ; Assert trajectory

  ;   :grow-count 1
  ;
  ;   ┌************┐
  ; 0 *............*
  ; 1 *............*
  ; 2 *............*
  ; 3 *............*
  ; 4 *............*
  ; 5 *.........@..*
  ; 6 *..oooooooo..*
  ; 7 *............*
  ; 8 *............*
  ; 9 *............*
  ; 0 *..........9.*
  ; 1 *............*
  ;   └************┘
  ;    012345678901

  ;//////////////////////////////////
  ;  API
  ;//////////////////////////////////
  (set-trajectory :up)
  (advance-turn)  

  ;//////////////////////////////////
  ;  Assertions
  ;//////////////////////////////////
  (is (= {:x 12 :y 12} (:size     @board)))            ; Assert board
  (is (= {:x 10 :y 10} (:position (:apple @board))))   ; Assert apple
  (is (= 9 (:val (:apple @board))))                   
  (is (= {:x  9 :y  5} (peek      (:position @worm)))) ; Assert head
  (is (= {:x  2 :y  6} (first     (:position @worm)))) ; Assert tail
  (is (= 1 (:grow-count  @worm)))
  (is (= :up @trajectory))                             ; Assert trajectory

  ;   :grow-count 0
  ;
  ;   ┌************┐
  ; 0 *............*
  ; 1 *............*
  ; 2 *............*
  ; 3 *............*
  ; 4 *............*
  ; 5 *.........o@.*
  ; 6 *..oooooooo..*
  ; 7 *............*
  ; 8 *............*
  ; 9 *............*
  ; 0 *..........9.*
  ; 1 *............*
  ;   └************┘
  ;    012345678901

  ;//////////////////////////////////
  ;  API
  ;//////////////////////////////////
  (set-trajectory :right)
  (advance-turn)  

  ;//////////////////////////////////
  ;  Assertions
  ;//////////////////////////////////
  (is (= {:x 12 :y 12} (:size     @board)))            ; Assert board
  (is (= {:x 10 :y 10} (:position (:apple @board))))   ; Assert apple
  (is (= 9 (:val (:apple @board))))                   
  (is (= {:x 10 :y  5} (peek      (:position @worm)))) ; Assert head
  (is (= {:x  2 :y  6} (first     (:position @worm)))) ; Assert tail
  (is (= 0 (:grow-count  @worm)))
  (is (= :right @trajectory))                          ; Assert trajectory

  ;   :grow-count 0
  ;
  ;   ┌************┐
  ; 0 *............*
  ; 1 *............*
  ; 2 *............*
  ; 3 *............*
  ; 4 *............*
  ; 5 *.........oo.*
  ; 6 *...ooooooo@.*
  ; 7 *............*
  ; 8 *............*
  ; 9 *............*
  ; 0 *..........9.*
  ; 1 *............*
  ;   └************┘
  ;    012345678901

  ;//////////////////////////////////
  ;  API
  ;//////////////////////////////////
  (set-trajectory :down)
  (advance-turn)  

  ;//////////////////////////////////
  ;  Assertions
  ;//////////////////////////////////
  (is (= {:x 12 :y 12} (:size     @board)))            ; Assert board
  (is (= {:x 10 :y 10} (:position (:apple @board))))   ; Assert apple
  (is (= 9 (:val (:apple @board))))                   
  (is (= {:x 10 :y  6} (peek      (:position @worm)))) ; Assert head
  (is (= {:x  3 :y  6} (first     (:position @worm)))) ; Assert tail
  (is (= 0 (:grow-count  @worm)))
  (is (= :down @trajectory))                           ; Assert trajectory

) ; w00t! ooooooooo@

