(ns ^:figwheel-always jiajiao.impl.html_handle)

(defn- bing-model-dispatch [node _ _]
  (if (= :input (first node))
    [(first node) (:type (get node 1))]
    (first node)))

(defmulti bing-model
          #'bing-model-dispatch
          :default nil)

(defmethod bing-model nil
  [node smap skey]
  (assoc-in node [2] (skey smap)))

(defmethod bing-model [:input "text"]
  [node smap skey]
  (assoc-in node
            [1 :on-change]
            #(swap! smap assoc skey (-> % .-target .-value))))

(defmethod bing-model [:input "radio"]
  [node smap skey]
  (assoc-in node
            [1 :on-change]
            #(swap! smap assoc skey (-> % .-target .-value))))

(defmethod bing-model [:input "checkbox"]
  [node smap skey]
  (assoc-in node
            [1 :on-change]
            #(if (-> % .-target .-checked)
              (swap! smap assoc skey (conj (set (get @smap skey)) (-> % .-target .-value)))
              (swap! smap assoc skey (disj (set (get @smap skey)) (-> % .-target .-value))))))

(defmethod bing-model :select
  [node smap skey]
  (assoc-in node
            [1 :on-change]
            #(swap! smap assoc skey (-> % .-target .-value))))

(defmethod bing-model :textarea
  [node smap skey]
  (assoc-in node
            [1 :on-change]
            #(swap! smap assoc skey (-> % .-target .-value))))