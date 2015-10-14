(ns simple.sudao3
  (:require [cljs-time.coerce :as coe]
            [cljs-time.format :as format]
            [reagent.core :as r]
            [ajax.core :refer [GET POST]]
            [jiajiao.widget :as wid]))

;some temp var
(def messlist (r/atom nil))
(def myself-info (r/atom nil))
(def touserinfo (r/atom nil))
(def CHARTTIME (format/formatter "HH:mm:ss"))
(def send-msg (r/atom ""))
(def messid (r/atom nil))
(def speak-state (r/atom false))

;some data handler
(defn binding-mess []
  (dotimes [i (count @messlist)]
    (let [messinfo (get @messlist i)
          call-back-fn (fn [res]
                         (swap! messlist
                                assoc-in
                                [i "mess"]
                                (get (js->clj res) "localId")))
          downconf {:serverId (get messinfo "mess")
                    :isShowProgressTips 0
                    :success call-back-fn}]
      (cond
        (= "pic" (get messinfo "type"))
        (.downloadImage js/wx (clj->js downconf))
        (= "voice" (get messinfo "type"))
        (.downloadVoice js/wx (clj->js downconf))))))


(defn init-wechat []
  (.ready js/wx #(binding-mess))
  (.error js/wx #(.log js/console %)))


(defn init-page-data []
  (let [sessid (.-value (.getElementById js/document "sessid"))
        topenid (.-value (.getElementById js/document "topenid"))
        myopenid (.-value (.getElementById js/document "myopenid"))]
    (POST "/index.php/addon/QdrugManager/Index/getCharInfo"
          {:params {:sessid sessid
                    :topenid topenid
                    :myopenid myopenid}
           :format :raw
           :response-format :json
           :handler #(do (reset! myself-info (get % "myinfo"))
                         (reset! touserinfo (get % "toinfo"))
                         (reset! messlist (get % "slist"))
                         (reset! messid sessid)
                         (init-wechat))})))

(defn send-handle [mess]
  (POST "/index.php/addon/QdrugManager/Index/sendmsg"
        {:params {:touser (get @touserinfo "id")
                  :mess mess
                  :messid @messid}
         :format :raw
         :response-format :json
         :handler #(if (= (get % "status") 1)
                    (swap! messlist conj {"id" (get % "id")
                                          "headimg" (get @myself-info "headimg"),
                                          "mess" mess,
                                          "myself" 1
                                          "type" "text"
                                          "ctime" (.getTime (js/Date.))}))})
  (reset! send-msg nil))

(defn send-img [url]
  (swap! messlist conj {
                        "headimg" (get @myself-info "headimg"),
                        "mess" url,
                        "myself" 1
                        "type" "pic"
                        "ctime" (.getTime (js/Date.))})
  (reset! send-msg nil))

(defn send-voice [local-id]
  (swap! messlist conj {
                        "headimg" (get @myself-info "headimg")
                        "mess" local-id
                        "myself" 1
                        "type" "voice"
                        "ctime" (.getTime (js/Date.))}))

(defn post-voice [sessid serverid touser]
  (POST "/index.php/addon/QdrugManager/Index/sendVoice"
        {:params {:sessid sessid
                  :mess serverid
                  :touser touser}
         :format :raw
         :response-format :json
         :handler #(swap! messlist
                          update-in
                          [(- (count @messlist) 1)]
                          assoc "id" (get % "messid"))}))

(defn post-image [sessid serverid touser]
  (POST "/index.php/addon/QdrugManager/Index/sendPic"
        {:params {:sessid sessid
                  :mess serverid
                  :touser touser}
         :format :raw
         :response-format :json
         :handler #(swap! messlist
                          update-in
                          [(- (count @messlist) 1)]
                          assoc "id" (get % "messid") "media_url" (get % "path"))}))

(defn upload-image [local-id]
  (let [suchandle (fn [res]
                    (post-image @messid
                                (get (js->clj res) "serverId")
                                (get @touserinfo "id")))
        config {:success suchandle
                :localId local-id
                :isShowProgressTips 0}]
    (.uploadImage js/wx (clj->js config))))

(defn upload-voice [local-id]
  (let [suchandle (fn [res]
                    (post-voice @messid
                                (get (js->clj res) "serverId")
                                (get @touserinfo "id")))
        config {:success suchandle
                :localId local-id
                :isShowProgressTips 0}]
    (.uploadVoice js/wx (clj->js config))))


(defn choose-image []
  (let [suchandle (fn [res]
                    (js/setTimeout #(upload-image (.toString (.-localIds res))) 100)
                    (send-img (.toString (.-localIds res))))
        config {:success suchandle
                :count 1}]
    (.chooseImage js/wx (clj->js config))))

(defn start-record []
  (.startRecord js/wx))

(defn stop-record []
  (let [succ #(do (upload-voice (get (js->clj %) "localId"))
                  (send-voice (get (js->clj %) "localId")))]
    (.stopRecord js/wx (clj->js {:success succ}))))

(defn on-voice-record-end []
  (let [succ #()]
    (.onVoiceRecordEnd js/wx (clj->js {:success succ}))))

(defn play-voice [local-id]
  (.playVoice js/wx (clj->js {:localId local-id})))

(defn speak [node]
  (let [nodedom (.-target node)]
    (if-not @speak-state
      (do
        (reset! speak-state true)
        (set! (.-innerHTML nodedom) "点击停止")
        (start-record))
      (do
        (reset! speak-state false)
        (set! (.-innerHTML nodedom) "点击录音")
        (stop-record)))))


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
      (= "voice" (get msg "type"))
      [:span [:a {:on-click #(play-voice (get msg "mess"))} "【点击播放】"]]
      (= "pic" (get msg "type"))
      [:span (wid/lazy-images {:data-src (get msg "mess")
                               :on-click (fn [] (.previewImage js/wx (clj->js {:current (get msg "mess")
                                                                               :urls (remove #(or (empty? %)
                                                                                                  (= -1 (.indexOf % ".jpg")))
                                                                                             (map #(get % "media_url")
                                                                                                  @messlist))})))})]
      :else [:span (get msg "mess")])]])

(defn other-msgtext [msg]
  [:div {:class (str "consult left " (if (= "pic" (get msg "type")) "mespic"))}
   [:img {:src (get msg "headimg") :alt ""}]
   [:div {:class "consult_text"}
    [:img {:src "/Addons/QdrugManager/View/default/Public/images/left.png"}]
    (cond
      (= "voice" (get msg "type"))
      [:span [:a {:on-click #(play-voice (get msg "mess"))} "【点击播放】"]]
      (= "pic" (get msg "type"))
      [:span (wid/lazy-images {:data-src (get msg "mess")
                               :on-click (fn [] (.previewImage js/wx (clj->js {:current (get msg "mess")
                                                                               :urls (remove #(or (empty? %)
                                                                                                  (= -1 (.indexOf % ".jpg")))
                                                                                             (map #(get % "media_url")
                                                                                                  @messlist))})))})]
      :else [:span (get msg "mess")])]])

(defn msgtext [mess]
  (if (= 1 (get mess "myself"))
    (myself-msgtext mess)
    (other-msgtext mess)))

(defn send-box []
  [:div {:class "sendbox"}
   [:div {:class "clear"}]
   [:div {:class "blank_100"}]
   [:div {:class "bottom"}
    [:div {:class "btngroup"}
     [:a#sendpic {:class "sendpicbtn linebtn"
                  :on-click choose-image} "发送图片"]
     [:a {:class "speakbtn linebtn"
          :on-click speak} "点击录音"]]
    [:div {:class "send"}
     [:input {:type "text"
              :id "messtextbox"
              :value @send-msg
              :on-change #(reset! send-msg (-> % .-target .-value))}]
     [:a {:id "sendbtn" :on-click #(send-handle @send-msg)} "发送"]]
    [:div {:class "clear"}]]])


(defn main []
  [:div {:class "my-gallery"}
   [speaking]
   [timespan (get (first @messlist) "ctime")]
   (for [mess @messlist]
     ^{:key mess} [msgtext mess])
   [send-box]])



(init-page-data)
(js/setInterval
  (fn []
    (POST "/index.php/addon/QdrugManager/Index/getlastmsg"
          {:params {:messid @messid}
           :format :raw
           :response-format :json
           :handler #(if (get % "status")
                      (let [messinfos (get % "mess")]
                        (doseq [messinfo messinfos]
                          (let [call-fn (fn [res]
                                          (swap! messlist
                                                 conj
                                                 (assoc
                                                   messinfo
                                                   "mess"
                                                   (get (js->clj res) "localId"))))
                                downconf (clj->js
                                           {:serverId (get messinfo "mess")
                                            :isShowProgressTips 0
                                            :success call-fn})]
                            (cond
                              (= "text" (get messinfo "type"))
                              (swap! messlist concat (get % "mess"))
                              (= "pic" (get messinfo "type"))
                              (.downloadImage js/wx downconf)
                              (= "voice" (get messinfo "type"))
                              (.downloadVoice js/wx downconf))))))})) 5000)


(defn ^:export init []
  (r/render [main] (.getElementById js/document "maincontent")))