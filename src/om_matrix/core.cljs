(ns om-matrix.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint    :refer [pprint]]))

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
  (println " counter main ---------------------------------")
  (om/add-root! reconciler
                Counter (gdom/getElement "app")))

(main)


;; ------------------------ scratch -------------

(def state-matrix-tree
  {:matrix {:rows [{:row/id 0
                    :cells [{:cell/id 0 :state :a} {:cell/id 1 :state :b}]}
                   {:row/id 1
                    :cells [{:cell/id 2 :state :c} {:cell/id 3 :state :d}]}]}})

(defui Cell
  static om/Ident
  (ident [this props]
         (let [id (:cell/id props)]
           (println "Cell.ident:: id: " id)
           `[:cell/id ~id]))
  static om/IQuery
  (query [this]
         [:cell/id :state])
  Object
  (render [this]
          (println "Cell.render:: props: " (om/props this))
          (let [{:keys [id state] :as datum} (om/props this)]
            (dom/div #js {:className "cell"} (name state)))))

(def cell (om/factory Cell {:keyfn :cell/id}))

(defui Row
  static om/Ident
  (ident [this props]
         (let [id (:row/id props)]
           (println "Row.ident:: id: " id)
           `[:row ~id]))
  static om/IQuery
  (query [this]
         `[{:cells ~(om/get-query Cell)}])
  Object
  (render [this]
          (println "Row.render:: props: " (om/props this))
          (let [{:keys [cells] :as props} (om/props this)]
            (dom/div #js {:className "row"}
                     (map cell cells)))))


(def row (om/factory Row {:keyfn :row/id}))

(declare reconciler-matrix)

(defui Matrix
  static om/Ident
  (ident [this props]
         (println "Matrix.ident:: props: " (pprint props))
         [:matrix])
  static om/IQuery
  (query [this]
         `[{:matrix [{:rows ~(om/get-query Row)}]}])
  Object
  (render [this]
          (println "Matrix.render:: props: " (om/props this))
          (let [{:keys [matrix] :as state} (om/props this)
                rows (:rows matrix)]
            (dom/div nil
                     (dom/div #js {:className "matrix"}
                              (map row rows))
                     (dom/button
                      #js {:onClick
                           (fn [e]
                             (om/transact! reconciler-matrix
                                           `[(game/move {})
                                             :matrix]))}
                      "Click me!")))))


(def matrix (om/factory Matrix))

(defn read-matrix [{:keys [state] :as env} key params]
  (println :--------------------------------------------------)
  (println "read:: key: " key " || state: " state " || params: " params)
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))


(defn mutate-matrix [{:keys [state] :as env} key params]
  (let [row-id (rand-int 2)
        cell-id (rand-int 2)
        val (keyword (char (+ 98 (rand-int 10))))
        state' (update-in @state [:matrix :rows row-id :cells cell-id :state] (fn [] val))]
    {:action #(reset! state state')}))

(def reconciler-matrix
  (om/reconciler {:state (atom state-matrix-tree)
                  :parser (om/parser {:read read-matrix
                                      :mutate mutate-matrix})}))

(defn main-matrix []
  (println "main-matrix ---------------------------------")
  (om/add-root! reconciler-matrix
                Matrix
                (gdom/getElement "scratch")))

(main-matrix)
