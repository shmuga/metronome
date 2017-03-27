(ns metronome.core
    (:require [reagent.core :as r]
              [metronome.components :as c]
              [cljs-bach.synthesis :as bach]
              [keybind.core :as key]))

(enable-console-print!)

(defonce context (bach/audio-context))


(def all-notes
  (r/atom {:four {:time 0.25}
           :eight {:time 0.125}
           :sixteen {:time 0.0625}}))

(def selected-note (r/atom :four))


(def tempo (r/atom 60))
(def accents [1 0 0 0])
(def play-note (r/atom (:time (@selected-note @all-notes))))

(defn calculate-fours-per-second [tempo]
  (/ 60 tempo))

(defn calculate-per-second [notes, fours-per-second]
  (/ 1 (* 4 notes fours-per-second)))


(def fours-per-second (r/atom (calculate-fours-per-second @tempo)))

(def per-second (r/atom (calculate-per-second @play-note @fours-per-second)))


(add-watch selected-note :selected-note #(reset! play-note (:time (@selected-note @all-notes))))

(add-watch tempo :tempo #(reset! fours-per-second (calculate-fours-per-second %4)))

(add-watch play-note :notes #(reset! per-second (calculate-per-second %4 @fours-per-second)))

(add-watch fours-per-second :fours #(reset! per-second (calculate-per-second @play-note %4)))


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


(defn on-note-select [selected-key]
  (reset! selected-note selected-key))



(defn child [name]
  [:div "Hi, I am " name
   [:br]
   [c/notes-list @all-notes @selected-note on-note-select]
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
            :on-change #(reset! tempo (-> % .-target .-value))}]])




(defn childcaller []
  [child "Foo Bar"])


(defn mountit []
  (r/render [childcaller]
            (.getElementById js/document "app")))

(mountit)
