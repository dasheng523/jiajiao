(ns simple.sudao3
  (:require [cljs-time.coerce :as coe]
            [cljs-time.format :as format]
            [reagent.core :as r]
            [ajax.core :refer [GET POST]]
            [jiajiao.widget :as wid]
            [goog.string :as gstring]))

;some temp var
(def messlist (r/atom []))
(def myself-info (r/atom nil))
(def touserinfo (r/atom nil))
(def CHARTTIME (format/formatter "HH:mm:ss"))
(def send-msg (r/atom ""))
(def messid (r/atom nil))
(def speak-state (r/atom false))
(def show-facebox (r/atom false))

(def face-map {
               0 "/::)",1 "/::~",2 "/::B",3 "/::|",4 "/:8-)",
               5 "/::<",6 "/::$",7 "/::X",8 "/::Z",9 "/::’(",
               10 "/::-|",11 "/::@",12 "/::P",13 "/::D",14 "/::O",
               15 "/::(",16 "/::+",17 "/:–b",18 "/::Q",19 "/::T",
               20 "/:,@P",21 "/:,@-D",22 "/::d",23 "/:,@o",24 "/::g",
               25 "/:|-)",26 "/::!",27 "/::L",28 "/::>",29 "/::,@",
               30 "/:,@f",31 "/::-S",32 "/:?",33 "/:,@x",34 "/:,@@",
               35 "/::8",36 "/:,@!",37 "/:!!!",38"/:xx",39 "/:bye",
               40 "/:wipe",41 "/:dig", 42 "/:handclap",43 "/:&-(",44 "/:B-)",
               45 "/:<@",46 "/:@>",47 "/::-O",48 "/:>-|",49 "/:P-(",
               50 "/::’|",51 "/:X-)",52 "/::*",53 "/:@x",54 "/:8*",
              55 "/:pd",56 "/:<W>",57 "/:beer",58 "/:basketb",59 "/:oo",
              60 "/:coffee",61 "/:eat",62 "/:pig",63 "/:rose",64 "/:fade",
              65 "/:showlove",66 "/:heart",67 "/:break",68 "/:cake",69 "/:li",
              70 "/:bome",71 "/:kn",72 "/:footb",73 "/:ladybug",74 "/:shit",
              75 "/:moon",76 "/:sun",77 "/:gift",78 "/:hug",79 "/:strong",
              80 "/:weak",81 "/:share",82 "/:v",83 "/:@)",84 "/:jj",
              85 "/:@@",86 "/:bad",87 "/:lvu",88 "/:no",89 "/:ok",
              90 "/:love",91 "/:<L>",92 "/:jump",93 "/:shake",94 "/:<O>",
              95 "/:circle",96 "/:kotow",97 "/:turn",98 "/:skip",99 "[挥手]",
              100 "/:#-0",101 "[街舞]",102 "/:kiss",103 "/:<&",104 "/:&>"
})

(defn- pictab [i]
  (str "<img src=\"/Addons/QdrugManager/View/default/Public/images/face/" i ".gif\">"))

(defn- replacefacepic [strp]
  (reduce #(clojure.string/replace %1 (last %2) (pictab (first %2))) strp face-map ))

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
                         (reset! messlist (into [] (get % "slist")))
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
   [:img {:src (get msg "headimg") :alt "" :class "headimg"}]
   [:div {:class "consult_text_ri"}
    [:img {:src "/Addons/QdrugManager/View/default/Public/images/right.png" :class "fiximg"}]
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
      :else [:span {:dangerouslySetInnerHTML {:__html (replacefacepic (get msg "mess"))}}])]])

(defn other-msgtext [msg]
  [:div {:class (str "consult left " (if (= "pic" (get msg "type")) "mespic"))}
   [:img {:src (get msg "headimg") :alt "" :class "headimg"}]
   [:div {:class "consult_text"}
    [:img {:src "/Addons/QdrugManager/View/default/Public/images/left.png" :class "fiximg"}]
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
      :else [:span {:dangerouslySetInnerHTML {:__html (replacefacepic (get msg "mess"))}}])]])

(defn msgtext [mess]
  (if (= 1 (get mess "myself"))
    (myself-msgtext mess)
    (other-msgtext mess)))

(defn check-face-event [i]
  (swap! send-msg str (face-map i) )
  (reset! show-facebox false))

(defn facebox []
  (if @show-facebox
    [:div.facebox
     [:div.facelist
      (for [i (range 100)]
        ^{:key i} [:a
                   {:class "faceitem"
                    :href "javascript:"
                    :on-click #(check-face-event i)}
                   [:img {:src (str "/Addons/QdrugManager/View/default/Public/images/face/" i ".gif")}]])
      ]]))

(defn send-box []
  [:div {:class "sendbox"}
   [:div {:class "clear"}]
   [:div {:class "blank_100"}]
   [:div {:class "bottom"}
    [facebox]
    [:div {:class "btngroup"}
     (if (empty? @send-msg)
       [:a#sendpic {:class "sendpicbtn linebtn"
                    :on-click choose-image} " "])
     [:a {:class "speakbtn linebtn"
          :on-click speak} " "]]
    [:div {:class "send"}
     [:input {:type "text"
              :id "messtextbox"
              :value @send-msg
              :on-change #(reset! send-msg (-> % .-target .-value))}]
     [:a {:class "facebtn" :href "javascript:" :on-click #(reset! show-facebox true)} " "]
     (if (not-empty @send-msg)
       [:a {:id "sendbtn" :on-click #(send-handle @send-msg)} "发送"])]
    [:div {:class "clear"}]]])


(defn main []
  [:div {:class "my-gallery"}
   [speaking]
   [timespan (get (first @messlist) "ctime")]
   (for [mess @messlist]
     ^{:key mess} [msgtext mess])
   [send-box]])



(init-page-data)
(def isfirst (r/atom true))
(js/setInterval
  (fn []
    (POST "/index.php/addon/QdrugManager/Index/getlastmsg"
          {:params {:messid @messid}
           :format :raw
           :response-format :json
           :handler #(if @isfirst
                      (reset! isfirst false)
                      (if (get % "status")
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
                                (swap! messlist conj messinfo)
                                (= "pic" (get messinfo "type"))
                                (.downloadImage js/wx downconf)
                                (= "voice" (get messinfo "type"))
                                (.downloadVoice js/wx downconf)))))))})) 5000)


(defn ^:export init []
  (r/render [main] (.getElementById js/document "maincontent")))
