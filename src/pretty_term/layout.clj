(ns pretty-term.layout
  (:refer-clojure :exclude [newline cat])
  (:require [clojure.string :as string]
            [clojure.walk :as w]))

(defn render-prims
  [[tag & params :as node]]
  (case tag
    :prim/nil ""
    :prim/text (let [[s d] params] (str s (render-prims d)))
    :prim/line (let [[i d] params] (str (apply str \newline (repeat i \space)) (render-prims d)))
    nil (throw (Exception. "Nil prim tag"))
    (throw (Exception. (format "Invalid prim tag %s in %s" tag (pr-str node))))))

(def newline [:prim/line 0 [:prim/nil]])

(defn text
  [& strs]
  (if (seq strs)
    (let [[s & ss] strs] [:prim/text s (apply text ss)])
    [:prim/nil]))

(defn nest
  [i prim-doc]
  (let [[prim-tag & params] prim-doc]
    (case prim-tag
      :prim/nil [:prim/nil]
      :prim/text (let [[s d] params] [:prim/text s (nest i d)])
      :prim/line (let [[j d] params] [:prim/line (+ i j) (nest i d)]))))

(defn cat*
  [pd1 pd2]
  (let [[prim-tag & params] pd1]
    (case prim-tag
      :prim/nil pd2
      :prim/text (let [[s d] params] [:prim/text s (cat* d pd2)])
      :prim/line (let [[i d] params] [:prim/line i (cat* d pd2)]))))

(defn cat
  [& pds]
  (if (seq pds)
    (let [[d & ds] pds] (cat* d (apply cat ds)))
    [:prim/nil]))

(defn lines
  [& docs]
  (apply cat (interpose newline (remove #(= % [:prim/nil]) docs))))
