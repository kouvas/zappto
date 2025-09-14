(ns zappto.ui.components
  (:require [clojure.string :as s]))

(defn service-card [{:keys [id service-name duration currency price details]} currencies state]
  (let [details-visible? (get-in state [:details-visibility id] false)
        details-text     (if details-visible? "Hide details" "Details")
        selected?        (= id (get-in state [:booking :selected-service]))
        animating?       (and (get-in state [:ui :animating-item])
                              (= id (get-in state [:ui :animating-item :id]))
                              (= :service (get-in state [:ui :animating-item :type])))]
    [:div.bg-base-100.shadow-md.mb-3.w-full.rounded-xl.overflow-hidden.hover:shadow-lg.transition-all.duration-200
     {:class (cond
               animating? ["ring-2" "ring-blue-500" "bg-blue-50" "scale-105" "transform"]
               selected? ["ring-2" "ring-blue-500"]
               :else [])}
     [:div.px-4.py-4.cursor-pointer.transition-all.duration-200
      {:class (when animating? ["pointer-events-none"])
       :on    {:click [[::animate-and-select-service id]]}}
      [:div
       [:h2.text-lg.font-semibold.mb-2.flex.items-center.transition-all.duration-200
        {:class (when animating? ["text-blue-600"])}
        (when (or selected? animating?) [:span.mr-2.text-blue-500 "✓"])
        service-name]]
      [:div.text-base
       [:span duration " mins • " [:span.font-medium (str (get currencies currency) price)]]
       (when details
         [:span " • "
          [:a.text-primary.hover:underline.cursor-pointer
           {:on {:click [[::stop-propagation] [::toggle-details id]]}}
           details-text]])]]

     (when (and details details-visible?)
       [:div.px-2.pb-2
        [:p details]])]))

(defn team-member-card [{:keys [id name surname img details]} state]
  (let [details-visible? (get-in state [:details-visibility id] false)
        details-text     (if details-visible? "Hide details" "Details")
        selected?        (= id (get-in state [:booking :selected-provider]))
        animating?       (and (get-in state [:ui :animating-item])
                              (= id (get-in state [:ui :animating-item :id]))
                              (= :provider (get-in state [:ui :animating-item :type])))]
    [:div.bg-base-100.shadow-md.mb-3.w-full.rounded-xl.overflow-hidden.hover:shadow-lg.transition-all.duration-200
     {:class (cond
               animating? ["ring-2" "ring-blue-500" "bg-blue-50" "scale-105" "transform"]
               selected? ["ring-2" "ring-blue-500"]
               :else [])}
     [:div.flex.items-center.px-4.py-4.cursor-pointer.transition-all.duration-200
      {:class (when animating? ["pointer-events-none"])
       :on    {:click [[::animate-and-select-provider id]]}}
      [:div.flex-shrink-0.mr-4
       [:img.h-12.w-12.rounded-full.object-cover
        {:src img
         :alt (str name " " surname)}]]
      [:div.flex-1
       [:div.font-medium.text-black.flex.items-center.transition-all.duration-200
        {:class (when animating? ["text-blue-600"])}
        (when (or selected? animating?) [:span.mr-2.text-blue-500 "✓"])
        (str name " " surname)]
       [:div.text-sm.text-gray-500 "Barber"]
       (when details
         [:a.text-primary.hover:underline.cursor-pointer
          {:on {:click [[::stop-propagation] [::toggle-details id]]}}
          details-text])]]
     (when (and details details-visible?)
       [:div.px-2.pb-2
        [:p details]])]))

(defn section [title & cards]
  [:div.mb-4
   [:h2.text-lg.font-medium.mb-2.scroll-mt-12 {:id (s/lower-case title)} title]
   [:div.flex.flex-col.gap-2
    cards]])

(defn top-navbar [state sections]
  (let [active   ["text-blue-600" "border-blue-500" "dark:border-blue-400" "dark:text-blue-300" "focus:outline-none"]
        inactive ["border-transparent" "dark:text-white" "cursor-base" "focus:outline-none" "hover:border-gray-400"]]
    [:div#navbar.flex.sticky.top-0.bg-slate-50.overflow-hidden.overflow-x-auto.scrollbar-hidden.border-b.border-gray-200.whitespace-nowrap.dark:border-gray-700
     (map (fn [{:keys [section-key]}]
            (let [name            (name section-key)
                  nav-link-hashed (str "#" name)]
              [:a.inline-flex.items-center.h-10.px-4.-mb-px.text-sm.text-center.text-gray-700.bg-transparent.border-b-4.sm:text-base.whitespace-nowrap
               {:class (if (= (:active-navlink state) nav-link-hashed)
                         active
                         inactive)
                :href  nav-link-hashed
                :on    {:click [[::set-active-navlink nav-link-hashed]]}}
               (s/capitalize name)]))
          sections)]))

(defn time-slot [{:keys [time]} state]
  (let [selected?  (= time (get-in state [:booking :selected-time]))
        animating? (and (get-in state [:ui :animating-item])
                        (= time (get-in state [:ui :animating-item :id]))
                        (= :time (get-in state [:ui :animating-item :type])))]
    [:button.px-4.py-2.rounded-lg.border.border-gray-300.text-sm.font-medium.transition-all.duration-200.hover:bg-gray-50
     {:class (cond
               animating? ["ring-2" "ring-blue-500" "bg-blue-50" "text-blue-600" "scale-105" "transform"]
               selected? ["ring-2" "ring-blue-500" "bg-blue-50" "text-blue-600"]
               :else ["text-gray-700"])
      :on    {:click [[::select-time time]]}}
     time]))

(defn time-slot-picker [time-slots state]
  (when (and (get-in state [:booking :selected-service])
             (get-in state [:booking :selected-provider])
             (get-in state [:booking :selected-date]))
    [:div.mt-6
     [:h3.text-lg.font-semibold.mb-4 "Select a time"]
     [:div.grid.grid-cols-3.gap-3
      (for [time time-slots]
        (time-slot {:time time} state))]]))

(defn booking-summary [services users currencies state]
  (when (and (get-in state [:booking :selected-service])
             (get-in state [:booking :selected-provider])
             (get-in state [:booking :selected-date])
             (get-in state [:booking :selected-time]))
    (let [selected-service-id  (get-in state [:booking :selected-service])
          selected-provider-id (get-in state [:booking :selected-provider])
          selected-date        (get-in state [:booking :selected-date])
          selected-time        (get-in state [:booking :selected-time])
          service              (first (filter #(= (:id %) selected-service-id) services))
          provider             (first (filter #(= (:id %) selected-provider-id) users))
          price                (get service :price)
          currency             (get service :currency)]
      [:div.mt-8.bg-gray-50.rounded-lg.p-6
       [:h3.text-xl.font-semibold.mb-4 "Booking Summary"]
       [:div.space-y-3
        [:div.flex.justify-between
         [:span.font-medium "Service:"]
         [:span (:service-name service)]]
        [:div.flex.justify-between
         [:span.font-medium "Barber:"]
         [:span (str (:name provider) " " (:surname provider))]]
        [:div.flex.justify-between
         [:span.font-medium "Date:"]
         [:span selected-date]]
        [:div.flex.justify-between
         [:span.font-medium "Time:"]
         [:span selected-time]]
        [:div.flex.justify-between.text-lg.font-semibold.border-t.pt-3
         [:span "Total:"]
         [:span (str (get currencies currency) price)]]]
       [:div.mt-6.flex.gap-3
        [:button.flex-1.bg-blue-500.hover:bg-blue-600.text-white.font-medium.py-3.px-4.rounded-lg.transition-colors
         {:data-confirm-booking true
          :on                   {:click [[::confirm-booking]]}}
         "Confirm Booking"]
        [:button.flex-1.bg-gray-200.hover:bg-gray-300.text-gray-700.font-medium.py-3.px-4.rounded-lg.transition-colors
         {:on {:click [[::reset-booking]]}}
         "Start Over"]]])))

(defn booking-confirmation-modal [state]
  (when (= (get-in state [:booking :status]) :confirmed)
    [:div.fixed.inset-0.bg-black.bg-opacity-50.flex.items-center.justify-center.z-50
     [:div.bg-white.rounded-lg.p-8.max-w-md.w-full.mx-4
      [:div.text-center
       [:div.text-green-500.text-6xl.mb-4 "✓"]
       [:h2.text-2xl.font-bold.mb-2 "Booking Confirmed!"]
       [:p.text-gray-600.mb-6 "Your appointment has been successfully booked. You will receive a confirmation email shortly."]
       [:button.bg-blue-500.hover:bg-blue-600.text-white.font-medium.py-3.px-6.rounded-lg.transition-colors
        {:on {:click [[::reset-booking]]}}
        "Book Another"]]]]))

(defn debug-booking-state [state]
  [:div.bg-yellow-100.p-2.m-2.rounded.text-xs
   [:div "Debug - Booking State:"]
   [:div "Selected Service: " (str (get-in state [:booking :selected-service]))]
   [:div "Selected Provider: " (str (get-in state [:booking :selected-provider]))]
   [:div "Selected Date: " (str (get-in state [:booking :selected-date]))]
   [:div "Status: " (str (get-in state [:booking :status]))]])
