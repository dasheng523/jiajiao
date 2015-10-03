(ns jiajiao.modules.navigation
  (:require-macros [secretary.core :refer [defroute]])
  (:require
    [reagent.core :as r]
    [goog.dom :as dom])
  (:import goog.History))


;创建一个目录
(defn- navigation [nav-data]
  (fn [props]
    [:ul
     [:li [:a {:href "#/"} "home"]]
     [:li [:a {:href "#/users"} "users"]]
     [:li [:a {:href "#/about"} "about"]]]))



(defn ^:export init []
  (r/render [navigation] (dom/getElement "nav")))