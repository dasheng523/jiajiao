(ns jiajiao.common
  (:require
    [cljs.reader :as reader]
    [reagent.core :as r]
    [jiajiao.impl.html_handle :as handle]))

(defn set-html! [el content]
  (set! (.-innerHTML el) content))


(defn text-onchange
  ([mmap mkey]
   #(swap! mmap assoc mkey (-> % .-target .-value)))
  ([mvar]
    #(reset! mvar (-> % .-target .-value))))

(defn radio-onchange
  ([mmap mkey]
   (let [isselect (r/atom false)]
     #(swap! mmap assoc mkey
             (if @isselect
               (do (reset! isselect false)
                   nil)
               (do (reset! isselect true)
                   (-> % .-target .-value))))))
  ([mvar]
    (let [isselect (r/atom false)]
      #(reset! mvar (if @isselect
                      (do (reset! isselect false)
                          nil)
                      (do (reset! isselect true)
                          (-> % .-target .-value)))))))








(def at (r/atom 0))

;输入一个vec，输出另一个vec，自动加入change函数
(defn input-template [vec-tepl]
  (when (vector? vec-tepl)
    (when (= (first vec-tepl) :input)
      (let [attr (first (filter map? vec-tepl))
            model (:model attr)
            newattr (assoc attr "onchange" #(reset! model (-> % .-target .-value)))]
        (swap! inc (reader/read-string model))
        (println newattr)))))

;先做单一的类型吧
;使用多重函数来实现不同的Input类型