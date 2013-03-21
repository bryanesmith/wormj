(defproject wormj "1.0.0"
  :description "Clojure port of BSD game `worm` - the growing worm game."
  :url "https://github.com/bryanesmith/wormj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clojure-lanterna "0.9.2"]
                 [org.clojure/tools.cli "0.2.2"]
                 [safely "1.0.0"]]
  :main wormj.core)
