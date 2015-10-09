(ns ^:figwheel-always simple.sudao
  (:require [cljs-time.coerce :as coe]
            [cljs-time.format :as format]
            [reagent.core :as r]
            [ajax.core :refer [GET POST]]
            [jiajiao.widget :as wid]))

(defn on-js-reload [])

(enable-console-print!)

;some temp var
(def messlist (r/atom nil))
(def myself-info (r/atom nil))
(def touserinfo (r/atom nil))
(def CHARTTIME (format/formatter "HH:mm:ss"))
(def send-msg (r/atom ""))
(def speak-state (r/atom false))

(defn speak [node]
  (let [nodedom (.-target node)]
    (if-not @speak-state
      (do
        (reset! speak-state true)
        (reset! (.-innerHTML nodedom) "点击停止"))
      (do
        (reset! speak-state false)
        (reset! (.-innerHTML nodedom) "点击录音")))))

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

(defn testpost []
  (GET "/data/test.json"
        {:params {"a5566" 117}
         :format :raw
         :handler #()}))

(testpost)

(defn send-handle [mess]
  (GET "/data/messhandle.json"
        {:params {:touser (get @touserinfo "id")
                  :mess mess}
         :response-format :json
         :handler #(swap! messlist conj {"id" (get % "id") ,
                                         "headimg" (get @myself-info "headimg"),
                                         "mess" mess,
                                         "myself" true
                                         "type" "text"
                                         "ctime" (.getTime (js/Date.))})})
  (reset! send-msg nil))


(defn send-img [url]
  (swap! messlist conj {
                         "headimg" (get @myself-info "headimg"),
                         "mess" url,
                         "myself" true
                         "type" "pic"
                         "ctime" (.getTime (js/Date.))})
  (reset! send-msg nil))

(defn receive-msg [mess]
  (if (vector? mess)
    (swap! messlist concat mess)
    (swap! messlist conj  mess)))

;some template makeup
(defn timespan [second]
  [:div {:class "history"}
   [:span (format/unparse CHARTTIME (coe/from-long (* second 1000)))]])

(defn speaking []
  (if @speak-state
    [:div {:class "speaking"} "正在录音"]))

(defn myself-msgtext [msg]
  [:div {:class (str "consult right " (if (= "pic" (get msg "type")) "mespic"))}
   [:img {:src (get msg "headimg") :alt ""}]
   [:div {:class "consult_text_ri"}
    [:img {:src "/Addons/QdrugManager/View/default/Public/images/right.png"}]
    (cond
      (= "text" (get msg "type")) [:span (get msg "mess")]
      (= "pic" (get msg "type"))
      [:span (wid/zoom-photo
               {:big (get msg "mess")
                :small (get msg "mess")})])]])

(defn other-msgtext [msg]
  [:div {:class (str "consult left " (if (= "pic" (get msg "type")) "mespic"))}
   [:img {:src (get msg "headimg") :alt ""}]
   [:div {:class "consult_text"}
    [:img {:src "/Addons/QdrugManager/View/default/Public/images/left.png"}]
    (cond
      (= "text" (get msg "type")) [:span (get msg "mess")]
      (= "pic" (get msg "type"))
      [:span (wid/zoom-photo
               {:big (get msg "mess")
                :small (get msg "mess")})])]])

(defn msgtext [mess]
  (if (get mess "myself")
    (myself-msgtext mess)
    (other-msgtext mess)))

(defn send-box []
  [:div {:class "sendbox"}
   [:div {:class "clear"}]
   [:div {:class "blank_100"}]
   [:div {:class "bottom"}
    [:div {:class "btngroup"}
     [:a#sendpic {:class "sendpicbtn linebtn"} "发送图片"]
     [:a {:class "speakbtn linebtn"} "按住说话"]]
    [:div {:class "send"}
     [:input {:type "text"
              :id "messtextbox"
              :value @send-msg
              :on-change #(reset! send-msg (-> % .-target .-value))}]
     [:a {:id "sendbtn" :on-click #(send-handle @send-msg)} "发送"]]
    [:div {:class "clear"}]]])


(defn main []
  [:div {:class "my-gallery"}
   [timespan (get (first @messlist) "ctime")]
   (for [mess @messlist]
     ^{:key mess} [msgtext mess])
   [send-box]
   [wid/photo-swipe]])

(def main-wrap
  (with-meta main
             {:component-did-mount
              (fn [_]
                (js/initPhotoSwipeFromDOM ".my-gallery")
                (js/Dropzone. "#sendpic"
                              (clj->js {"url" "/index.html"
                                        "previewTemplate" "<div></div>"
                                        "thumbnail"
                                        #(send-img %2)})))}))

(.config js/wx {:ddd "111"})
(.error js/wx #(js/alert %))
(.ready js/wx #(js/alert "ok"))

#_(js/setInterval
  (fn []
    (GET "/data/test.json"
          {:params {:messid 1}
           :format :raw
           :response-format :json
           :handler #(if (%)
                      (js/alert "555"))})) 1000)

(init-page-data)

;some test code
(defn testupload []
  [:div {:id "upload"} "上传"])

(defn testli [w]
  (if (pos? w)
    [:li w])
  )

(defn testfor []
  [:ul
   (for [w (range 5)] ^{:key w} [testli w])])

(def testupload2
  (with-meta testupload
             {:component-did-mount
              (fn [_]
                (js/Dropzone. "#upload"
                              (clj->js {"url" "/index.html"
                                        "previewTemplate" "<div></div>"
                                        "thumbnail"
                                        #(set!
                                          (.-innerHTML (.getElementById js/document "maincontent2"))
                                          (str "<img src='" %2 "'>"))})))}))

(def testmouse
  ())

(defn testzoom []
  [:div {:id "test"}
   [:div {:class "my-gallery"}
    (wid/zoom-photo
      {:big "http://assets.jq22.com/plugin/pc-120a23b0-2db6-11e4-954d-000c29f61318.png"
       :small "http://www.aspjzy.com/softimg/2013/09/20131010171515.jpg"})
    [:figure
     [:a {:href "http://r3.ykimg.com/050E000051D3DF916758392CB4050D3C"
          :itemprop "contentUrl"
          :data-size "1024x1024"}
      [:img {:src "http://i-7.vcimg.com/crop/5000f0e1e087e0ff9a5628f5c4eb86b336216%28600x%29/thumb.jpg"
             :itemprop "thumbnail"}]]]]
   [wid/photo-swipe]])

(defn testimage []
  [:ul
   [:li
    (wid/lazy-images {:data-src "http://www.pp3.cn/uploads/allimg/111120/10110A360-1.jpg"})]
   [:li
    (wid/lazy-images {:data-src "http://g.hiphotos.baidu.com/zhidao/pic/item/fc1f4134970a304eeb387208d0c8a786c9175c2e.jpg"})]])



(defn ^:export init []
  (r/render [speaking] (.getElementById js/document "maincontent")))