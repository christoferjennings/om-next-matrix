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

;; ------------------------ scratch -------------

(def init-state
  {:board [[1 2]
           [3 4]]})

(defmulti read om/dispatch)

(defmethod read :board
  [{:keys [state] :as env} key params]
  {:value (key state)})

(defmethod read :default
  [{:keys [state] :as env} key params]
  {:value {:value (str "no-value-for-key: " key)}})

(defui Cell
  static om/Ident
  (ident [this props]
         [:board/cell])
  static om/IQuery
  (query [this]
         `[:cell])
  Object
  (render [this]
          (dom/div nil "cell")))

(def cell (om/factory Cell))
;(cell)

(defui Board
  static om/IQuery
  (query [this]
         (let [subquery (om/get-query Cell)]
           `[{:board ~subquery}])))

(def board (om/factory Board))


(def norm-d (om/tree->db Board init-state true))
norm-d

(def parser2 (om/parser {:read read}))
(parser {:state (atom norm-d)} '[:board])

(om/get-query Cell)
