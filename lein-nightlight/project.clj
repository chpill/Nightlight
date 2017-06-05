(defproject org.clojars.chpill/lein-nightlight "1.6.5-SNAPSHOT"
  :description "A conveninent Nightlight launcher for Leiningen projects"
  :url "https://github.com/chpill/Nightlight"
  :license {:name "Public Domain"
            :url "http://unlicense.org/UNLICENSE"}
  :dependencies [[org.clojars.chpill/nightlight "1.6.5-SNAPSHOT" :exclusions [org.clojure/core.async]]
                 [leinjacker "0.4.2"]
                 [org.clojure/tools.cli "0.3.5"]]
  :eval-in-leiningen true)

