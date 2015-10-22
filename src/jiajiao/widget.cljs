(ns ^:figwheel-always jiajiao.widget
  (:require [reagent.core :as r]))

(defn lazy-images [attr]
  (let [lazy-config {:src "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw=="
                     :on-load #(js/lzld (.-target %))}
        el-attrs (merge lazy-config attr)]
    [:img el-attrs]))


(defn zoom-photo [{:keys [big small]}]
  [:figure
   [:a {:href big
        :itemprop "contentUrl"
        :data-size "1024x1024"}
    (lazy-images {:data-src small :itemprop "thumbnail"})]])

(defn photo-swipe []
  [:div {:class "pswp" :tabindex "-1" :role "dialog" :aria-hidden "true"}
   [:div {:class "pswp__bg"}]
   [:div {:class "pswp__scroll-wrap"}
    [:div {:class "pswp__container"}
     [:div {:class "pswp__item"}]
     [:div {:class "pswp__item"}]
     [:div {:class "pswp__item"}]]
    [:div {:class "pswp__ui pswp__ui--hidden"}
     [:div {:class "pswp__top-bar"}
      [:div {:class "pswp__counter"}]
      [:button {:class "pswp__button pswp__button--close" :title "Close (Esc)"}]
      [:button {:class "pswp__button pswp__button--share" :title "Share"}]
      [:button {:class "pswp__button pswp__button--fs" :title "Toggle fullscreen"}]
      [:button {:class "pswp__button pswp__button--zoom" :title "Zoom in/out"}]
      [:div {:class "pswp__preloader"}
       [:div {:class "pswp__preloader__icn"}
        [:div {:class "pswp__preloader__cut"}
         [:div {:class "pswp__preloader__donut"}]]]]]
     [:div {:class "pswp__share-modal pswp__share-modal--hidden pswp__single-tap"}
      [:div {:class "pswp__share-tooltip"}]]
     [:button {:class "pswp__button pswp__button--arrow--left"
            :title "Previous (arrow left)"}]
     [:button {:class "pswp__button pswp__button--arrow--right"
               :title "Next (arrow right)"}]
     [:div {:class "pswp__caption"}
      [:div {:class "pswp__caption__center"}]]]]])