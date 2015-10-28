(ns ^:figwheel-always jiajiao.core
    (:require [reagent.core :as reagent]
              [jiajiao.learn.modalwindow :as modalwindow]))



(defn ^:export main []
  (reagent/render [modalwindow/home]
                  (.getElementById js/document "app")))


(defn on-js-reload [])

(fn my-flatten [c]
  (reverse (reduce (fn [colls info]
                     (if (coll? info)
                       (concat colls (my-flatten info))
                       info)) '() c)))

