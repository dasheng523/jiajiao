(ns jiajiao.learn.modalwindow
  (:require [reagent-modals.modals :as reagent-modals]))


(defn modal-window-button []
  [:div.btn.btn-primary
   {:on-click #(reagent-modals/modal! [:div "some message to the user!"])}
   "My Modal"])


(defn home []
  [:div
   [reagent-modals/modal-window]
   ;; ATTNETION \/
   [modal-window-button]
   ;; ATTENTION /\
   ])

