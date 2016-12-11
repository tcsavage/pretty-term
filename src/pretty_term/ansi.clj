(ns pretty-term.ansi)

(defn ansi-code
  [code]
  (str \u001b \[ code \m))

(def colors
  {:black 0
   :red 1
   :green 2
   :yellow 3
   :blue 4
   :magenta 5
   :cyan 6
   :white 7
   :default 9})

(def color-targets
  {:fg 3
   :bg 4})

(def specials
  {:reset 0
   :bold 1
   :underline 4
   :inverse 7})

(defn color-code
  [target color]
  (ansi-code (str (color-targets target) (colors color))))

(defn special-code
  [special]
  (ansi-code (specials special)))
