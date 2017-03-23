(ns metronome.core
    (:require [reagent.core :as r]
              [cljs-bach.synthesis :as bach]
              [keybind.core :as key]))

(enable-console-print!)

(println "This text is printed from src/metronome/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:interval "Hello world!"}))

(defonce context (bach/audio-context))

(def tempo (r/atom 60))
(def accents [0 0 0 0])
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

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

(defn ping [freq]
  (bach/connect->
    (bach/add
      (bach/sine (+ freq 2))
      (bach/sine freq))         ; Try a sawtooth wave.
    (bach/percussive 0.01 0.04))) ; Try varying the attack and decay.


(defn sound
  "docstring"
  [freq]
  (-> (ping freq)
      (bach/connect-> bach/destination)
      (bach/run-with context (bach/current-time context) 1)))


(key/bind! "k" ::k #(sound 2093))
(key/bind! "a" ::a #(sound 1760))
(key/bind! "l" ::l #(sound 540))
(key/bind! "i" ::i #(sound 240))
(key/bind! "n" ::n #(sound 110.000))
(key/bind! "k" ::k #(sound 2240))
(key/bind! "m" ::m #(sound 4440))

(def counter (atom -1))

(defn play-sound []
  (reset! counter (mod (+ @counter 1) (count accents)))
  (if (= (accents @counter) 0)
    (sound 1760)
    (sound 2093)))


(defn play-infinite-sound []
  (play-sound)
  (if @is-working
    (js/setTimeout play-infinite-sound (/ 1000 @per-second))
    ()))

(defn start-interval []
  (reset! is-working true)
  (play-infinite-sound))

(defn stop-interval []
  (reset! is-working false))


(defn child [name]
  [:p "Hi, I am " name
   [:br]
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
