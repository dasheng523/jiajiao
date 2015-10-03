(ns ^:figwheel-always simple.sudao
  (:require [cljs-time.coerce :as coe]
            [cljs-time.core :as cjstime]
            [cljs-time.format :as format]
            [reagent.core :as r]
            [goog.dom :as dom]
            [ajax.core :refer [GET POST]]
            [jiajiao.impl.html_handle :as handle]))

(defn on-js-reload [])

;some temp var
(def messlist (r/atom nil))
(def myself-info (r/atom nil))
(def touserinfo (r/atom nil))
(def CHARTTIME (format/formatter "HH:mm:ss"))
(def send-msg (r/atom ""))



;some data handler
(defn init-page-data []
  (GET "/data/myselfinfo.json"
       {:params {}
        :handler #(reset! myself-info %)})
  (GET "/data/mess.json"
       {:params {}
        :handler #(reset! messlist %)})
  (GET "/data/touserinfo.json"
       {:params {}
        :handler #(reset! touserinfo %)}))

(defn send-handle [mess]
  (GET "/data/messhandle.json"
        {:params {:touser (get @touserinfo "id")
                  :mess mess}
         :response-format :json
         :handler #(swap! messlist conj {"id" (get % "id") ,
                                         "headimg" (get @myself-info "headimg"),
                                         "mess" mess,
                                         "myself" true
                                         "ctime" (.getTime (js/Date.))})})
  (reset! send-msg nil))


;some template makeup
(defn timespan [second]
  [:div {:class "history"}
   [:span (format/unparse CHARTTIME (coe/from-long (* second 1000)))]])

(defn myself-msgtext [msg]
  [:div {:class "consult right"}
   [:img {:src (get msg "headimg") :alt ""}]
   [:div {:class "consult_text_ri"}
    [:img {:src "/Addons/QdrugManager/View/default/Public/images/right.png"}]
    [:span (get msg "mess")]]])

(defn other-msgtext [msg]
  [:div {:class "consult left"}
   [:img {:src (get msg "headimg") :alt ""}]
   [:div {:class "consult_text"}
    [:img {:src "/Addons/QdrugManager/View/default/Public/images/left.png"}]
    [:span (get msg "mess")]]])

(defn send-box []
  [:div {:class "sendbox"}
   [:div {:class "clear"}]
   [:div {:class "blank_100"}]
   [:div {:class "bottom"}
    [:div {:class "send"}
     [:input {:type "text"
              :id "messtextbox"
              :value @send-msg
              :on-change #(reset! send-msg (-> % .-target .-value))}]
     [:a {:id "sendbtn" :on-click #(send-handle @send-msg)} "发送"]]
    [:div {:class "clear"}]]])

(defn main []
  [:div {}
   (cons [timespan (get (first @messlist) "ctime")]
         (into []
               (map (fn [msg] (if (get msg "myself")
                                (myself-msgtext msg)
                                (other-msgtext msg)))
                    @messlist)))
   [send-box]])


(init-page-data)

;some test code
(def userinfo (r/atom {}))
(defn sample []
  [:div {}
   (handle/bing-model [:input {:type "checkbox" :name "textbox" :value "radio1"}] userinfo :name)
   (handle/bing-model [:input {:type "checkbox" :name "textbox" :value "radio2"}] userinfo :name)
   (handle/bing-model [:select {:id "sele"}
                       [:option {:value "111"} "111"]
                       [:option {:value "222"} "222"]
                       [:option {:value "333"} "333"]] userinfo :address)])


(js/IScroll. "#maincontent")

(defn ^:export init []
  (r/render [main] (dom/getElement "maincontent")))