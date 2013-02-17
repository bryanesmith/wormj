# wormj

Clojure port of BSD game `worm` - the growing worm game.

## NOTICE

The game is not playable (by humans), though the API is functional. I will update this notice as progress continues.

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

But if you would rather attempt launch a Swing-based GUI environment (but fall back on text-based console):

    lein run -t auto

## API

We will start by creating a 12x12 board. By chance (though you can override chance), the apple, `2`, randomly appeared two spaces to the right of the worm.

    (require 'wormj.state :reload 
             'wormj.functions)

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

    (init-game 12 12)

Note that the default trajectory is `:right`, so to advance one space closer to the apple, just advance the turn:

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

    (advance-turn)

And to consume the apple, advance the turn again:

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

    (advance-turn)  

Since the worm consumed the apple, two things happen:
  1. A new apple is randomly generated. In this example, an apple with nutrional value of `9` appears at the `{:x 10 :y 10}`.
  2. The worm will grow to match the nutritional value of the consumed apple. The last apple, `2`, had nutritional value of 2, so for the next two turns the worm will grow.

You can determine how many turns has to grow:

    (:grow-count @wormj.state/worm)

Now we will turn up. Our worm grows a new segment, and our `:grow-count` is decremented:

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

    (set-trajectory :up)
    (advance-turn)  

We will complete growing and take a `:right`:

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

    (set-trajectory :right)
    (advance-turn)  

Finally, we turn `:down`. Since are no longer growing, our tail starts moving again.

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

    (set-trajectory :down)
    (advance-turn)  

Note that this application has three layers:
  1. Functional layer (`src/wormj/functions.clj`): pure functions.
  2. State layer (`src/wormj/state.clj`): holds state such as current worm and board, and other violations of pure functions (such as random generation).
  3. GUI layer (`src/wormj/core.clj`): reads input, uses above API, and renders output from state.

For more examples, see associated tests.

## History

  * **0.2** (2013/02/16): Functional API, but no GUI.
  * **0.1** (2013/02/12): Game is non-functional while working on documentation and infrastructure.

## License

Need to figure this out.
