(ns jiajiao.modules.navigation
  (:require-macros [secretary.core :refer [defroute]])
  (:require
    [reagent.core :as r]
    [secretary.core :as secretary]
    [goog.events :as events]
    [goog.dom :as dom])
  (:import goog.History))

(def app (dom/getElement "app"))

(defn- set-html! [el content]
  (set! (.-innerHTML el) content))

(defroute home-path "/" []
          (set-html! app "home"))

(defroute users-path "/users" []
          (set-html! app "users"))

(defroute about-path "/about" []
          (set-html! app "about"))

(defroute "*" []
          (set-html! app "<h1>Not Found</h1>"))

;创建一个目录
(defn- navigation [nav-data]
  (fn [props]
    [:ul
     [:li [:a {:href "#/"} "home"]]
     [:li [:a {:href "#/users"} "users"]]
     [:li [:a {:href "#/about"} "about"]]]))


(defn history-bing
  []
  (secretary/set-config! :prefix "#")
  (let [history (History.)]
    (events/listen history "navigate"
                   (fn [event]
                     (secretary/dispatch! (.-token event))))
    (.setEnabled history true)))




(defn ^:export init []
  (history-bing)
  (r/render [navigation] (js/document.getElementById "nav")))