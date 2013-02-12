# wormj

Clojure port of BSD game `worm` - the growing worm game.

## NOTICE

This game is not working yet. I will remove this notice when it is. =)

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

## History

  * **0.1** (2013/02/12): Game is non-functional while working on documentation and infrastructure.

## License


