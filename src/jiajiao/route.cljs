(ns jiajiao.route
  (:require-macros [secretary.core :refer [defroute]])
  (:require
    [secretary.core :as secretary]
    [goog.events :as events]
    [goog.dom :as dom]
    [jiajiao.common :as common]
    [jiajiao.modules.login-form :as login])
  (:import goog.History))

(def app (dom/getElement "app"))

(defroute "/" []
          (common/set-html! app "home"))

(defroute "/users" []
          (common/set-html! app "users"))

(defroute "/login" []
          (login/init))

(defroute "/about" []
          (common/set-html! app "about"))

(defroute "*" []
          (common/set-html! app "<h1>Not Found</h1>"))


(defn history-bing
  []
  (secretary/set-config! :prefix "#")
  (let [history (History.)]
    (events/listen history "navigate"
                   (fn [event]
                     (secretary/dispatch! (.-token event))))
    (.setEnabled history true)))

