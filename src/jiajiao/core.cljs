(ns ^:figwheel-always jiajiao.core
    (:require [jiajiao.modules.navigation :as nav]
              [jiajiao.route :as route]))


(route/history-bing)
(nav/init)

(defn on-js-reload [])


