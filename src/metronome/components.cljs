(ns metronome.components
  (:require [goog.string :as gstring]))

(defn get-note-icon [type]
  (gstring/unescapeEntities
    (case type
      :four "\u2669"
      :eight "\u266B"
      :sixteen "\u266C")))

(defn single-note [type note click]
  (let [styles {:width "60px"
                :color (if (:is-selected note)
                          "#2196F3"
                          "#424242")
                :cursor "pointer"
                :lineHeight "60px"
                :fontSize "60px"
                :transition "color .5s ease-in-out"
                :textAlign "center"
                :height "60px"}]
    [:div {:on-click #(click type)
           :style styles}
          (get-note-icon type)]))


(defn notes-list [notes on-select]
  [:div {:style
         {:display "flex"}}
   (map
     #(single-note (first %) (second %) on-select)
     notes)])
