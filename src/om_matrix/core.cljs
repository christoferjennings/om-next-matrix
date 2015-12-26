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

(def state-matrix
  {:matrix [[:row 1]
            [:row 2]]
   :row {1 [[:cell 1] [:cell 2]]
         2 [[:cell 3] [:cell 4]]}
   :cell {1 {:id 1 :state :x}
          2 {:id 2 :state :o}
          3 {:id 3 :state :x}
          4 {:id 4 :state :o}}})

(defui Cell
  ;; static om/Ident
  ;; (ident [this props]
  ;;        (let [id (:id props)]
  ;;          (println "Cell.ident: id: " id)
  ;;          `[[:cell ~id]]))
  ;; static om/IQuery
  ;; (query [this]
  ;;        (println "Cell.query: " (om/props this))
  ;;        [:cell])
  Object
  (render [this]
          (let [{:keys [id state] :as datum} (om/props this)]
            (println "Cell: " (om/props this))
            (dom/div #js {:className "cell"} (name state)))))

(def cell (om/factory Cell))

(defui Row
  Object
  (render [this]
          (let [{:keys [row-id state]} (om/props this)
                row (:row state)
                cell-links (get row row-id)
                cell-ids (map second cell-links)
                cell-data (map (fn [id] (get (:cell state) id)) cell-ids)]
            (println "Row: " (om/props this))
            ;; (println "Row: row-id: " row-id)
            ;; (println "Row: row: " row)
            ;; (println "Row: cell-links: " cell-links)
            ;; (println "Row: cell-ids: " cell-ids)
            (dom/div #js {:className "row"}
                     (map cell cell-data)))))

(def row (om/factory Row))

(defui Matrix
  ;; static om/IQuery
  ;; (query [this]
  ;;        (let [subquery (om/get-query Cell)]
  ;;          `[{:board ~subquery}]))
  Object
  (render [this]
          (let [{:keys [matrix] :as state} (om/props this)]
            (println "Matrix: " (om/props this))
            (dom/div #js {:className "matrix"}
                     (map #(row {:row-id (second %) ; <- the row state is weird, second gets the id
                                 :state state
                                 }) matrix))))
  )


(def matrix (om/factory Matrix))

(def reconciler-matrix
  (om/reconciler {:state state-matrix}))

(defn main-matrix []
  (println "main-matrix ---------------------------------")
  (om/add-root! reconciler-matrix
                Matrix (gdom/getElement "scratch")))

(main-matrix)
