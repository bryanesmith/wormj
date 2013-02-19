# wormj

Clojure port of BSD game `worm` - the growing worm game.

## Notice

The game is finished. `ooooooo@`

## Build & run

1. Install [Leiningen](http://leiningen.org/#install)

2. Run:

        lein deps
   
3. You can run the application using Leiningen:

        lein run

  Or you can build an Uberjar and run that:

        lein uberjar
        java -jar wormj-<version>-standalone.jar

## Usage

You can run the application without any options, and the game will attempt to run inside a text-based terminal (Unix or Cygwin):

    lein run

However, you can specify a terminal in which to run:

    lein run -t <terminal>

Terminal options include:
  * `auto`: attempt to use Swing-based (GUI) environment, otherwise use text-based console
  * `swing`: use Swing-based (GUI) environment
  * `text`: use text-based console. Attempts to use `unix` or `cygwin`
  * `unix`
  * `cygwin`

So the default is the same as:

    lein run -t text

But if you would rather launch a Swing-based GUI environment (but fall back on text-based console):

    lein run -t auto

## API

Let's start by initializing a game, which generates a 12x12 board, along with a worm and an apple. 

    (require 'wormj.functions 
             'wormj.state
             'wormj.core :reload)

    (wormj.state/init-game 12 12)
    (wormj.core/print-game)

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

A couple notes about `print-game`:
  * `print-game` is intended for debugging, but is not used for rendering during game play
  * The output of `print-game` in this section (which is shown as `; comments` below the Clojure code examples) is slightly enhanced for the sake of clarity.

By chance (though you can always override chance), the apple, `2`, randomly appears two spaces to the right of the worm.

Note that the default trajectory is `:right`, so to advance one space closer to the apple, just advance the turn:

    (wormj.state/advance-turn)
    (wormj.core/print-game)

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

And to consume the apple, advance the turn again:

    (wormj.state/advance-turn)
    (wormj.core/print-game)

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

Since the worm consumes the apple, two things happen:
  1. A new apple is randomly generated. In this example, an apple `9` appears at the `{:x 10 :y 10}` position on the board.
  2. The worm will grow to match the nutritional value of the consumed apple. The last apple, `2`, had nutritional value of 2, so for the next two turns the worm will grow.

Note that you can determine how many more turns your worm will grow:

    (:grow-count @wormj.state/worm)

This turn, our worm will move up. Our worm also grows this turn, and hence its `:grow-count` is decremented:

    (wormj.state/set-trajectory :up)
    (wormj.state/advance-turn)
    (wormj.core/print-game)

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

Our worm now takes a `:right`, and grows one last time (until the next apple is consumed):

    (wormj.state/set-trajectory :right)
    (wormj.state/advance-turn)  
    (wormj.core/print-game)

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

Finally, the worm turns `:down`. Since it is no longer growing, the worm's tail starts moving again.

    (wormj.state/set-trajectory :down)
    (wormj.state/advance-turn)  
    (wormj.core/print-game)

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

Note that this application has three layers:
  1. *Functional layer* (`src/wormj/functions.clj`): pure functions.
  2. *State layer* (`src/wormj/state.clj`): holds the game state, such as current worm and board. Also holds any impure functions (such as functions with side-effects or random generation).
  3. *GUI layer* (`src/wormj/core.clj`): reads input, calls API, and renders output from state. Also provides functionality like `game-to-str` and `print-game` for debugging purposes.

For more examples, see associated tests.

## History

  * **1.0** (2013/02/18): Game completed.
  * **0.3** (**alpha**, 2013/02/17): Game functional (including arrow keys, `hjkl`, and `HJKL`), though score not reported until game over, and script arguments ignored.
  * **0.2** (2013/02/16): Functional API, but no GUI.
  * **0.1** (2013/02/12): Game is non-functional while working on documentation and infrastructure.

## License

Need to figure this out.
