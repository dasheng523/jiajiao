(ns jiajiao.learn.datepicker
  (:require [reagent.core :as reagent]))


(defn home-render []
  [:input {:type "text" :placeholder "click to show datepicker"}])

(defn home-did-mount [this]
  (.datepicker (js/$ (reagent/dom-node this)) (clj->js {:format "dd/mm/yyyy"})))

(defn home []
  (reagent/create-class {:reagent-render home-render
                         :component-did-mount home-did-mount}))

(fn [p & params]
  (apply #(if (> %1 %2) %1 %2)
         p
         params))

(max 1 [5 48 9])