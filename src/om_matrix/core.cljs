(ns om-matrix.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint    :refer [pprint]]))

(enable-console-print!)

(def
  ^{:doc "This is what I want for the raw matrix."}
  state-matrix-raw
  {:matrix [[:a :b]
            [:c :d]]})

(def
  ^{:doc "I think this is needed as an intermediate state so the cells have ids."}
  state-matrix-intermediate
  {:matrix [[{:cell/id 0 :state :a} {:cell/id 1 :state :b}]
            [{:cell/id 2 :state :c} {:cell/id 3 :state :d}]]})

(def
  ^{:doc "I think the db version of the matrix could be."}
  state-matrix-db
  {:matrix [[[:cell/by-id 0] [:cell/by-id 1]]
            [[:cell/by-id 2] [:cell/by-id 3]]]
   :cell/by-id {0 {:id 0 :state :a}
                1 {:id 1 :state :b}
                2 {:id 2 :state :c}
                3 {:id 3 :state :d}}})

(def
  ^{:doc "Can the db have vectors as 'compound ids'?."}
  state-matrix-db-compound-ids
  {:matrix [[[:cell/by-id [0 0]] [:cell/by-id [0 1]]]
            [[:cell/by-id [1 0]] [:cell/by-id [1 1]]]]
   :cell/by-id {[0 0] {:id [0 0] :state :a}
                [0 1] {:id [0 1] :state :b}
                [1 0] {:id [1 0] :state :c}
                [1 1] {:id [1 1] :state :d}}})

(def
  ^{:doc "This is the state used for now. But the mutate doesn't have any way
          to reference the cells on their own so the whole matrix has to re-render"}
  state-matrix-tree
  {:matrix {:rows [{:row/id 0
                    :cells [{:cell/id 0 :state :a} {:cell/id 1 :state :b}]}
                   {:row/id 1
                    :cells [{:cell/id 2 :state :c} {:cell/id 3 :state :d}]}]}})

(defn read
  "This is taken straight from the om.next quick start.
   I'm pretty sure the final version will need custom readers
   but so far I haven't got them to work either, so this is
   the simplest 'fail' :)"
  [{:keys [state] :as env} key params]
  (println :--------------------------------------------------)
  (println "read:: key: " key " || state: " state " || params: " params)
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defn mutate
  "Randomly mutate the matrix so that one of the cells gets a new value."
  [{:keys [state] :as env} key params]
  (let [row-id (rand-int 2)
        cell-id (rand-int 2)
        key [:matrix :rows row-id :cells cell-id :state]
        current-val (get-in @state key)
        val (keyword (char (+ 98 (rand-int 20))))
        state' (update-in @state key (fn [] val))]
    (if (not (= val current-val))
      {:action #(reset! state state')}
      (mutate env key params))))

(def parser (om/parser {:read read
                               :mutate mutate}))

(def reconciler
  (om/reconciler {:state (atom state-matrix-tree)
                  :parser parser}))

(defui Cell
  ;; ;; Ident doesn't affect anything yet. This is just my best guess so far.
  ;; static om/Ident
  ;; (ident [this props]
  ;;        (let [id (:cell/id props)]
  ;;          (println "Cell.ident:: id: " id)
  ;;          `[:cell/id ~id]))
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
  ;; ;; Ident doesn't affect anything yet. This is just my best guess so far.
  ;; static om/Ident
  ;; (ident [this props]
  ;;        (let [id (:row/id props)]
  ;;          (println "Row.ident:: id: " id)
  ;;          `[:row ~id]))
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

(defui Matrix
  ;; ;; Ident doesn't affect anything yet. This is just my best guess so far.
  ;; static om/Ident
  ;; (ident [this props]
  ;;        (println "Matrix.ident:: props: " (pprint props))
  ;;        [:matrix])
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
                             (om/transact! reconciler
                                           `[(matrix/change {})
                                             :matrix]))}
                      "Mutate!")))))


(def matrix (om/factory Matrix))

(defn main []
  (println "main ---------------------------------")
  (om/add-root! reconciler
                Matrix
                (gdom/getElement "app")))
