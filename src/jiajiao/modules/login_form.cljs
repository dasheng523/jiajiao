(ns jiajiao.modules.login-form
  (:require
    [goog.dom :as dom]
    [reagent.core :as r]))

(def user-state (r/atom {}))

(defn- login []
  (fn [props]
    [:form {:method "post" :action ""}
     [:h1 (:username @user-state)]
     [:input {:type "input"
              :name "username"
              :on-change #(swap! user-state assoc :username (-> % .-target .-value))}]
     [:input {:type "password"
              :name "password"
              :on-change #(swap! user-state assoc :password (-> % .-target .-value))}]
     [:button {:type "submit"} "登录"]]))

(defn init []
  (r/render [login] (dom/getElement "app")))
