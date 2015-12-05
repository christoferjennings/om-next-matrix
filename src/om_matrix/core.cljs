(ns om-matrix.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(def app-state (atom {:count 0}))

(defui Counter
  Object
  (render [this]
          (let [{:keys [count]} (om/props this)]
            (dom/div nil
                     (dom/span nil (str "Count: " count))
                     (dom/button
                      #js {:onClick
                           (fn [e]
                             (swap! app-state update-in [:count] inc))}
                      "Click me!")))))

(defn read
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
            {:value :not-found})))

(def parser (om.next/parser {:read read}))

(def reconciler
  (om/reconciler {:state app-state}))

(defn main []
  (println "main ---------------------------------")
  (om/add-root! reconciler
                Counter (gdom/getElement "app")))

(main)
(println "Hello worldxxxx!")
