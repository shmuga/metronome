(ns metronome.core
    (:require [reagent.core :as r]
              [metronome.components :as c]
              [cljs-bach.synthesis :as bach]
              [keybind.core :as key]))

(enable-console-print!)

(defonce context (bach/audio-context))

(def tempo (r/atom 60))
(def accents [1 0 0 0])
(def notes (r/atom (/ 1 16)))

(defn calculate-fours-per-second [tempo]
  (/ 60 tempo))

(defn calculate-per-second [notes, fours-per-second]
  (/ 1 (* 4 notes fours-per-second)))


(def fours-per-second (r/atom (calculate-fours-per-second @tempo)))

(def per-second (r/atom (calculate-per-second @notes @fours-per-second)))


(add-watch tempo :tempo #(reset! fours-per-second (calculate-fours-per-second %4)))

(add-watch notes :notes #(reset! per-second (calculate-per-second %4 @fours-per-second)))

(add-watch fours-per-second :fours #(reset! per-second (calculate-per-second @notes %4)))


(def is-working (atom false))


(defn sound-seq [freq]
  (bach/connect->
    (bach/add
      (bach/sine (+ freq 2))
      (bach/sine freq))         ; Try a sawtooth wave.
    (bach/percussive 0.01 0.04))) ; Try varying the attack and decay.

(defn make-sound
  "docstring"
  [freq]
  (-> (sound-seq freq)
      (bach/connect-> bach/destination)
      (bach/run-with context (bach/current-time context) 1)))

(def accent-counter (atom -1))

(defn play-metronom-sound []
  (reset! accent-counter (mod (+ @accent-counter 1) (count accents)))
  (if (= (accents @accent-counter) 0)
    (make-sound 1760)
    (make-sound 2093)))


(defn play-infinite-sound []
  (play-metronom-sound)
  (if @is-working
    (js/setTimeout play-infinite-sound (/ 1000 @per-second))
    ()))

(defn start-interval []
  (reset! is-working true)
  (play-infinite-sound))

(defn stop-interval []
  (reset! is-working false))

(def all-notes
  (r/atom {:four {:time 0.25 :is-selected true}
           :eight {:time 0.125 :is-selected false}
           :sixteen {:time 0.0625 :is-selected false}}))



(defn on-note-select [selected-key]
  (map
    #(swap! all-notes
              update-in
                [(first %)]
                assoc :is-selected (= selected-key (first %)))
    @all-notes))



(defn child [name]
  [:div "Hi, I am " name
   [:br]
   [c/notes-list @all-notes on-note-select]
   [:button
    {:on-click start-interval}
    "Start!"]
   [:button
    {:on-click stop-interval}
    "Stop!"]
   [:br]
   [:input {
            :type "number"
            :value @tempo
            :on-change #(reset! tempo (-> % .-target .-value))}]
   [:br]
   [:input {
            :type "text"
            :value @notes
            :on-change #(reset! notes (-> % .-target .-value))}]])




(defn childcaller []
  [child "Foo Bar"])


(defn mountit []
  (r/render [childcaller]
            (.getElementById js/document "app")))

(mountit)
