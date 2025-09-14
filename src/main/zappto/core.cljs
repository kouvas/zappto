(ns zappto.core
  (:require [clojure.walk :as walk]
            [replicant.dom :as r]
            [zappto.ui.pages :as pages]
            [zappto.router :as router]
            [zappto.ui.components :as comp]))

(defonce !store (atom {:active-navlink "#services"
                       :current-route  nil
                       :booking        {:selected-service  nil
                                        :selected-provider nil
                                        :selected-date     nil
                                        :selected-time     nil
                                        :status            :selecting-service}
                       :ui             {:animating-item     nil
                                        :show-calendar-info false}
                       :navigation     {:previous-route nil}}))

(defn render-ui [state]
  (if-let [match (:current-route state)]
    (pages/render-page state match)
    "page not found"))

(defn effect-execute!
  "Executes side effects on the application state atom.
   Takes a state atom and an effect vector (containing effect type and arguments),
   and performs the corresponding state mutation.

   State changes trigger UI updates through atom watchers."
  [store [effect & args]]
  (case effect
    :effect/toggle-details
    (swap! store update-in [:details-visibility (first args)] not)

    :effect/toggle-calendar-info
    (swap! store update-in [:ui :show-calendar-info] not)

    :effect/set-active-navlink
    (swap! !store assoc :active-navlink (first args))

    :effect/run-scroll-observer
    (let [observer (js/IntersectionObserver.
                     (fn [entries _]
                       (doseq [entry (array-seq entries)]
                         (when (.-isIntersecting entry)
                           (let [id        (.. entry -target -id)
                                 hash-link (str "#" id)]
                             (effect-execute! !store [:effect/set-active-navlink hash-link])))))
                     #js {:root       nil
                          :rootMargin "-20% 0px -70% 0px"
                          :threshold  0.1})]
      (doseq [section (array-seq (.querySelectorAll js/document "h2[id]"))]
        (.observe observer section)))

    :effect/navigate
    (let [[route-name params] args
          current-route (get-in @store [:current-route :data :name])]
      (swap! !store assoc-in [:navigation :previous-route] current-route)
      (router/navigate! route-name params)
      (js/setTimeout #(.scrollTo js/window 0 0) 100))

    :effect/navigate-back
    (let [state          @store
          previous-route (get-in state [:navigation :previous-route])
          current-route  (get-in state [:current-route :data :name])]
      (cond
        (and previous-route (not= previous-route current-route))
        (do
          (router/navigate! previous-route)
          (js/setTimeout #(.scrollTo js/window 0 0) 100))

        :else
        (do
          (swap! !store assoc :booking {:selected-service  nil
                                        :selected-provider nil
                                        :selected-date     nil
                                        :selected-time     nil
                                        :status            :selecting-service})
          (swap! !store assoc :details-visibility {})
          (swap! !store assoc-in [:navigation :previous-route] nil)
          (router/navigate! :route/homepage)
          (js/setTimeout #(.scrollTo js/window 0 0) 100))))

    :effect/on-route-change
    (let [new-route  (first args)
          route-name (get-in new-route [:data :name])]
      (swap! !store assoc :current-route new-route)
      (when (= route-name :route/homepage)
        (swap! !store assoc :booking {:selected-service  nil
                                      :selected-provider nil
                                      :selected-date     nil
                                      :selected-time     nil
                                      :status            :selecting-service})
        (swap! !store assoc :details-visibility {})))

    :effect/stop-propagation
    (let [evt (first args)]
      (when evt
        (.stopPropagation evt)))

    :effect/select-service
    (swap! !store assoc-in [:booking :selected-service] (first args))

    :effect/select-provider
    (swap! !store assoc-in [:booking :selected-provider] (first args))

    :effect/select-date
    (swap! !store assoc-in [:booking :selected-date] (first args))

    :effect/select-time
    (let [time (first args)]
      (swap! !store assoc-in [:booking :selected-time] time)
      (js/setTimeout
        (fn []
          (when-let [confirm-btn (.querySelector js/document "[data-confirm-booking]")]
            (.scrollIntoView confirm-btn #js {:behavior "smooth" :block "center"})
            (.add (.-classList confirm-btn) "animate-pulse")
            (js/setTimeout #(.remove (.-classList confirm-btn) "animate-pulse") 2000)))
        100))

    :effect/confirm-booking
    (swap! !store assoc-in [:booking :status] :confirmed)

    :effect/reset-booking
    (do
      (swap! !store assoc :booking {:selected-service  nil
                                    :selected-provider nil
                                    :selected-date     nil
                                    :selected-time     nil
                                    :status            :selecting-service})
      (swap! !store assoc :details-visibility {})
      (swap! !store assoc-in [:navigation :previous-route] nil)
      (router/navigate! :route/homepage)
      (js/setTimeout #(.scrollTo js/window 0 0) 100))

    :effect/animate-and-select-service
    (let [service-id    (first args)
          state         @store
          current-route (get-in state [:current-route :data :name])
          has-provider? (get-in state [:booking :selected-provider])
          next-route    (if has-provider? :route/calendar :route/team)]
      (swap! !store assoc-in [:navigation :previous-route] current-route)
      (swap! !store assoc-in [:ui :animating-item] {:type :service :id service-id})
      (swap! !store assoc-in [:booking :selected-service] service-id)
      (js/setTimeout
        (fn []
          (swap! !store assoc-in [:ui :animating-item] nil)
          (router/navigate! next-route)
          (.scrollTo js/window 0 0))
        500))

    :effect/animate-and-select-provider
    (let [provider-id   (first args)
          state         @store
          current-route (get-in state [:current-route :data :name])
          has-service?  (get-in state [:booking :selected-service])
          next-route    (if has-service? :route/calendar :route/services)]
      (swap! !store assoc-in [:navigation :previous-route] current-route)
      (swap! !store assoc-in [:ui :animating-item] {:type :provider :id provider-id})
      (swap! !store assoc-in [:booking :selected-provider] provider-id)
      (js/setTimeout
        (fn []
          (swap! !store assoc-in [:ui :animating-item] nil)
          (router/navigate! next-route)
          (.scrollTo js/window 0 0))
        500))

    :effect/animate-and-select-date
    (let [date-str         (first args)
          state            @store
          current-route    (get-in state [:current-route :data :name])
          has-service?     (get-in state [:booking :selected-service])
          has-provider?    (get-in state [:booking :selected-provider])
          should-navigate? (or (not has-service?) (not has-provider?))
          next-route       (cond
                             (not has-service?) :route/services
                             (and has-service? (not has-provider?)) :route/team
                             :else :route/calendar)]
      ;; Only update previous-route if we're actually navigating
      (when should-navigate?
        (swap! !store assoc-in [:navigation :previous-route] current-route))
      (swap! !store assoc-in [:ui :animating-item] {:type :date :id date-str})
      (swap! !store assoc-in [:booking :selected-date] date-str)
      (js/setTimeout
        (fn []
          (swap! !store assoc-in [:ui :animating-item] nil)
          (when should-navigate?
            (router/navigate! next-route)
            (.scrollTo js/window 0 0)))
        500))))

(defn action->effect
  "Transforms action data into effect data that can be processed by the system.
   Takes the current application state, the DOM event object, and a collection of action data,
   and returns a flattened sequence of effect vectors.
   Action -> Effect

   Returns empty sequence for unknown actions after logging to console."
  [state event actions]
  (mapcat
    (fn [action]
      (or false                                        ;; (some-ns/a-function state action)
          (case (first action)
            ::comp/toggle-details
            [(into [:effect/toggle-details] (rest action))]

            ::comp/toggle-calendar-info
            [[:effect/toggle-calendar-info]]

            ::comp/stop-propagation
            [[:effect/stop-propagation event]]

            ::comp/set-active-navlink
            [(into [:effect/set-active-navlink] (rest action))]

            ::pages/run-scroll-observer
            [[:effect/run-scroll-observer]]

            ::comp/navigate
            [(into [:effect/navigate] (rest action))]

            ::comp/navigate-back
            [[:effect/navigate-back]]

            ::comp/select-service
            [(into [:effect/select-service] (rest action))]

            ::comp/select-provider
            [(into [:effect/select-provider] (rest action))]

            ::comp/select-date
            [(into [:effect/select-date] (rest action))]

            ::comp/select-time
            [(into [:effect/select-time] (rest action))]

            ::comp/confirm-booking
            [[:effect/confirm-booking]]

            ::comp/reset-booking
            [[:effect/reset-booking]]

            ::comp/animate-and-select-service
            [(into [:effect/animate-and-select-service] (rest action))]

            ::comp/animate-and-select-provider
            [(into [:effect/animate-and-select-provider] (rest action))]

            ::comp/animate-and-select-date
            [(into [:effect/animate-and-select-date] (rest action))]

            (prn "Unknown action"))))
    actions))

(defn interpolate-actions
  "Processes data structures by replacing special keywords with their corresponding values.
   Takes a DOM event object and a data structure, then walks through the data structure
   recursively, replacing special keywords with actual values from the event.

   Returns a new data structure with all special keywords interpolated.
   Leaves all other values unchanged."
  [event data]
  (walk/postwalk
    (fn [x]
      (case x
        :event.target/value
        (some-> event .-target .-value)

        x))
    data))

(defn on-route-change!
  "Handler for route changes. Updates the app state with the new route."
  [match]
  (effect-execute! !store [:effect/on-route-change match]))

(defn on-navigate
  "Called when route changes. Updates app state with new route information."
  [match history-state]
  (on-route-change! match))

(defn init!
  "Init -> State -> State Watcher -> UI Event ->
  Interpolation -> Action -> Effect -> Effect Execution -> UI Update"
  []
  (router/init-router! on-navigate)

  (add-watch !store ::render
             (fn [_ _ _ new-state]
               (r/render
                 js/document.body
                 (render-ui new-state))))

  (r/set-dispatch!
    (fn [{:replicant/keys [dom-event trigger life-cycle]} event-data]
      (js/console.log "dom event: " dom-event "trigger: " trigger "lifecycle: " life-cycle "event data: " event-data)
      (->> (interpolate-actions dom-event event-data)
           (action->effect @!store dom-event)
           (run! #(effect-execute! !store %)))))

  ;; Trigger the initial render
  (swap! !store assoc ::initialised-at (.getTime (js/Date.))))

(comment

  @!store
  (require '[dataspex.core :as ds])
  (ds/inspect "Store" @!store)

  (:path (router/match-by-path "/services"))
  (router/match-by-name :route/team)

  (router/navigate! :route/services))
