(ns pretty-term.core
  (:require [pretty-term.dsl :as dsl]
            [pretty-term.layout :as layout]))

(defn render
  [dsl]
  (->> dsl
       dsl/eval
       layout/render-prims))
