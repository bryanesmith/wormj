(ns wormj.core
  (:require [wormj.functions   :as f]
            [wormj.state       :as s]
            [lanterna.terminal :as t]
            [clojure.tools.cli :as c]
            [clojure.stacktrace])
  (:gen-class))
; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 

; === STATE ===
(def t-last-move-ms  (ref nil))   ; timestamp prev move

; === RENDERING OPTIMIZATIONS ===
(def prev-apple (ref nil)) 
(def prev-worm  (ref nil))
(def prev-score (ref nil))

; === SETTINGS ===
(def term (ref nil)) 
(def board-char (char \space))    ; Empty position
(def debug? false)                ; Show tracers?
(def move-timeout-ms 1000)        ; ms before auto turn advance

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn tracer [str]
  (if debug?
    (println "[DEBUG]" str)))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~  
(defn to-str-map-key
  "Given a point, creates key."
  [point]
  (keyword (str (:x point) "_" (:y point))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn to-str-map
  "Returns a map of maps with character data. (x_y (game-to-stto-str-map))"
  [worm board]
  (let [ apple      (:apple board)
         apple-key  (to-str-map-key (:position apple)) 
         apple-char (char (+ 48 (:val apple)))
         pos        (:position worm)
         head       (peek pos)
         head-key   (to-str-map-key head)
         head-char  \@
         body       (pop pos)
         body-char  \o]
  (into { apple-key apple-char head-key head-char } 
        (map #(vector (to-str-map-key %) body-char) body))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn wall-to-str
  "Renders top or bottom wall to string"
  [type width]
  (case type
    :top    (str "┌" (apply str (take width (repeat "*"))) "┐")
    :bottom (str "└" (apply str (take width (repeat "*"))) "┘")
    (throw (AssertionError. (str "Invalid wall type: " type)))))

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
  (let [board   @wormj.state/board
        worm    @wormj.state/worm
        width   (get-in board [:size :x])
        height  (get-in board [:size :y])
        str-map (to-str-map worm board)]
    (flatten
      (vector
        (wall-to-str :top width)  
        (for [y (range 0 height)]
          (str "*" (row-to-str y width str-map) "*"))
        (wall-to-str :bottom width)))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn print-game
  "Print out results of game-to-str"
  []
  (println (clojure.string/join "\n" (game-to-str))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn error [e]
  (if debug?
    (.printStackTrace e)
    (do
      (println)
      (println "[error]" (.getMessage e))
      (println)))
  (System/exit 1))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn print-game-over-msg
  ""
  []

  ; Standard game summary
  (println)
  (println "Well, you ran into something and the game is over.")
  (println "Your final score was" (f/score @s/worm))

  ; Tracers for debugging
  (tracer (str "Trajectory: " @s/trajectory))
  (println))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn draw-board
  "Draw the board to the terminal"
  []
  (let [b-w (get-in @s/board [:size :x])
        b-h (get-in @s/board [:size :y])]

    ; "Worm"
    (t/move-cursor @term 1 0)
    (t/put-string @term "Wormj")

    ; Top wall
    (t/move-cursor @term 0 1)
    (t/put-string @term (wall-to-str :top b-w))

    ; Side walls
    (loop [i 0]
      (when (< i b-h)
        (t/move-cursor @term 0 (+ 2 i))
        (t/put-string @term "*")
        (t/move-cursor @term (+ b-w 1) (+ 2 i) )
        (t/put-string @term "*")
        (recur (inc i))))

    ; Bottom wall
    (t/move-cursor @term 0 (+ b-h 2))
    (t/put-string @term (wall-to-str :bottom b-w))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
; Source: http://goo.gl/Teu2N 
; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn subtract-lists[a b]
   (remove (into #{} b) a))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn draw-board-character
  "Draw character at specified board (not screen) coord."
  [c coord]
  (t/move-cursor @term (+ 1 (:x coord)) (+ 2 (:y coord)))
  (t/put-character @term c))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn draw-updates
  "Updates screen: worm, apple, score"
  []
  (let [head    (peek (:position @s/worm))
        score   (f/score @s/worm)
        b-width (get-in @s/board [:size :x])]
    
    ; Render score
    (if (and (> score 0)
             (or (nil? @prev-score)
                 (= 0 @prev-score)))
      (do 
        (t/move-cursor @term (- b-width 9) 0)
        (t/put-string @term "Score:")))

    (if (and (not= score @prev-score)
             (> score 0))
      (do
        (t/move-cursor @term (- b-width 3) 0)
        (t/put-string @term (format "%4d" score))))

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
    (draw-board-character \@ head)
    (t/move-cursor @term (+ 1 (:x head)) (+ 2 (:y head)))

    ; Render apple (if necessary)
    (if (or (nil? @prev-apple)
            (not= @prev-apple (:apple @s/board)))
      (let [apple (:apple @s/board)
            val   (:val   apple)
            pos   (:position apple)]
        (draw-board-character (char (+ 48 val)) pos)))
    
    ; Store ref to worm
    (dosync
      (ref-set prev-worm  @s/worm)
      (ref-set prev-apple (:apple @s/board))
      (ref-set prev-score score))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn update-t-last-move []
  (dosync
    (ref-set t-last-move-ms (System/currentTimeMillis))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn move-timeout? 
  "Returns true if user hasn't moved in required time, and should advance turn automatically" 
  []
  (if-not (nil? @t-last-move-ms)
    (>= (- (System/currentTimeMillis) @t-last-move-ms) move-timeout-ms)
    false))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn handle-move [way turns]
  (update-t-last-move)
  (s/set-trajectory way)
  (let [apple (:apple @s/board)]
    (loop [i 0]
      ; Break when i turns or apple consumed
      (when (and
              (< i turns)
              (not (f/game-over? @s/worm @s/board))
              (= apple (:apple @s/board)))
        (s/advance-turn)
        (recur (inc i))))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn handle-key-press [key]
  (case key
    \h     (handle-move :left  1)
    \j     (handle-move :down  1)
    \k     (handle-move :up    1)
    \l     (handle-move :right 1)

    \H     (handle-move :left  9)
    \J     (handle-move :down  5)
    \K     (handle-move :up    5)
    \L     (handle-move :right 9)

    :left  (handle-move :left  1)
    :down  (handle-move :down  1)
    :up    (handle-move :up    1)
    :right (handle-move :right 1)

    ; Default, do nothing
    nil))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn run-in-term
  "Handles execution game once terminal is stated"
  []
  (let [term-size (t/get-size @term)
        board-x   (- (first term-size) 2)
        board-y   (- (last term-size)  3)]
    (wormj.state/init-game board-x board-y)
    (draw-board)
    (loop [] ; Loop until game over
      (if-not (f/game-over? @s/worm @s/board)
        (do
          (draw-updates)         
          (handle-key-press (t/get-key @term))
          (. Thread (sleep 10)) 
          (if (move-timeout?)
            (do
              (update-t-last-move)
              (s/advance-turn)))
          (recur))))))

; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn set-terminal [terminal]
  (if-not (some #(= terminal %) ["auto" "swing" "text" "unix" "cygwin"])
    (throw (Exception. (str "Invalid terminal: " terminal))))
  (dosync (ref-set term (t/get-terminal (keyword terminal)))))
 
; ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
(defn -main
  "User runs game."
  [& args]
  (let [[options args banner]
        (c/cli args
               ["-t" "--terminal" "Specify terminal" :default "text"])]
    
    (try 
      ; === Handle args ===
      (set-terminal (:terminal options))

      ; === Start game ===
      (t/start @term)
      (run-in-term)

    (catch Exception e (error e))

    (finally 
      (do
        (if-not (nil? @term)
          (t/stop @term))
        (try (print-game-over-msg) (catch Exception e)))))))

