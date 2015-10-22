(ns ^:figwheel-always simple.sudao
  (:require [cljs-time.coerce :as coe]
            [cljs-time.format :as format]
            [reagent.core :as r]
            [ajax.core :refer [GET POST]]
            [jiajiao.widget :as wid]))

(defn on-js-reload [])

(enable-console-print!)

(def messlist (r/atom []))

(defn messdon [node]
  [:li (str node)])

(defn testipn []
  [:ul
   (for [mess @messlist]
     ^{:key mess} [messdon mess])])


(defn ^:export init []
  (r/render [testipn] (.getElementById js/document "maincontent")))