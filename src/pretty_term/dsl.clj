(ns pretty-term.dsl
  (:refer-clojure :exclude [eval])
  (:require [pretty-term.ansi :as a]
            [pretty-term.layout :as l]))

(defn eval-node-dispatch
  [ansi-base node]
  (first node))

(declare eval)

(defmulti eval-node eval-node-dispatch)

(defmethod eval-node :text
  [ansi-base [_ & strings]]
  (apply l/text strings))

(defmethod eval-node :lines
  [ansi-base [_ & lines]]
  (apply l/lines (map (partial eval ansi-base) lines)))

(defmethod eval-node :nest
  [ansi-base [_ i & nodes]]
  (l/cat*
    ;; Start-off a nested block with an indent.
    (l/text (apply str (repeat i \space)))
    (l/nest i (apply l/cat (map (partial eval ansi-base) nodes)))))

(defmethod eval-node :align
  [ansi-base [_ i & nodes]]
  (l/nest i (apply l/cat (map (partial eval ansi-base) nodes))))

(defmethod eval-node :cat
  [ansi-base [_ & nodes]]
  (apply l/cat (map (partial eval ansi-base) nodes)))

(defn ansi-node
  [ansi-base code nodes]
  (l/cat
    (l/text code)
    (apply l/cat (map #(eval (str ansi-base code) %) nodes))
    (l/text ansi-base)))

(defmethod eval-node :bold
  [ansi-base [_ & nodes]]
  (ansi-node ansi-base (a/special-code :bold) nodes))

(defmethod eval-node :italic
  [ansi-base [_ & nodes]]
  (ansi-node ansi-base (a/special-code :italic) nodes))

(defmethod eval-node :underline
  [ansi-base [_ & nodes]]
  (ansi-node ansi-base (a/special-code :underline) nodes))

(defmethod eval-node :color
  [ansi-base [_ color & nodes]]
  (ansi-node ansi-base (a/color-code :fg color) nodes))

(defmethod eval-node :background
  [ansi-base [_ color & nodes]]
  (ansi-node ansi-base (a/color-code :bg color) nodes))

(defmethod eval-node :default-style
  [ansi-base [_ & nodes]]
  (ansi-node ansi-base (a/special-code :reset) nodes))

(defn bullets
  [ansi-base bullet-node nodes]
  [:nest 2 (into [:lines] (for [node nodes] [:cat bullet-node [:align 2 node]]))])

(defmethod eval-node :bullets
  [ansi-base [_ & nodes]]
  (eval ansi-base (bullets ansi-base [:default-style "• "] nodes)))

(defn eval
  ([x] (eval (a/special-code :reset) x))
  ([ansi-base x]
   (cond
     (nil? x)    [:prim/nil]
     (string? x) (l/text x)
     (vector? x) (eval-node ansi-base x)
     :else       (l/text (pr-str x)))))
