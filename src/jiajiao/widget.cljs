(ns ^:figwheel-always jiajiao.widget
  (:require [reagent.core :as r]
            [lazyload]))

(defn lazy-images [attr]
  (let [lazy-config {:src "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw=="
                     :on-load #(js/lzld (.-target %))}
        el-attrs (merge lazy-config attr)]
    [:img el-attrs]))