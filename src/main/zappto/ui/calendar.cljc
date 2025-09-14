(ns zappto.ui.calendar
  (:require [tick.core :as t]
            [clojure.string]
            [zappto.ui.components :as comp]
            [zappto.ui.mock :as mock]
            #?(:cljs [zappto.ui.icons :as icons])))

(def day-initials ["M" "T" "W" "T" "F" "S" "S"])

(defn get-month-data [year month]
  (let [first-day (t/date (str year "-" (if (< month 10) (str "0" month) month) "-01"))
        first-day-weekday (keyword (str (t/day-of-week first-day)))
        days-to-subtract (case first-day-weekday
                           :MONDAY 0
                           :TUESDAY 1
                           :WEDNESDAY 2
                           :THURSDAY 3
                           :FRIDAY 4
                           :SATURDAY 5
                           :SUNDAY 6)
        start-date (t/<< first-day (t/new-period days-to-subtract :days))
        today (t/today)
        two-weeks-from-today (t/>> today (t/new-period 14 :days))]

    (->> (range 42)
         (map #(t/>> start-date (t/new-period % :days)))
         (partition 7)
         (map (fn [week]
                (map (fn [date]
                       (let [day-num (t/day-of-month date)
                             current-month (t/month date)
                             is-current-month (= (t/int current-month) month)
                             is-today (= date today)
                             is-past (t/< date today)
                             is-too-far-future (t/> date two-weeks-from-today)
                             is-available (and is-current-month
                                               (not is-past)
                                               (not is-too-far-future))]
                         {:day day-num
                          :date date
                          :current-month? is-current-month
                          :today? is-today
                          :past? is-past
                          :too-far-future? is-too-far-future
                          :available? is-available
                          :classes (cond
                                     is-today ["bg-blue-500" "text-white" "font-medium"]
                                     (and is-current-month is-available) ["text-gray-900" "hover:bg-gray-100" "cursor-pointer"]
                                     (and is-current-month (or is-past is-too-far-future)) ["text-gray-400" "cursor-not-allowed"]
                                     :else ["text-gray-300" "cursor-not-allowed"])}))
                     week))))))

(def current-month-data
  (let [today (t/today)]
    (get-month-data (t/int (t/year today)) (t/int (t/month today)))))

(defn calendar-header [state]
  (let [today (t/today)
        month-name (-> today t/month str clojure.string/lower-case clojure.string/capitalize)
        year (t/int (t/year today))
        show-info? (get-in state [:ui :show-calendar-info] false)]
    [:div.flex.justify-between.items-center.mb-4
     [:div.flex.items-center.gap-2
      [:h1.text-lg.font-medium.text-gray-900 (str month-name " " year)]
      [:div.relative
       [:button.p-1.rounded-full.transition-colors.text-gray-500 {:class ["hover:bg-gray-100" "hover:text-gray-700"]
                                                                  :on {:click [[::comp/toggle-calendar-info]]}}
        #?(:cljs icons/info-icon
           :clj [:svg.w-4.h-4 {:viewBox "0 0 24 24" :fill "none" :stroke "currentColor" :stroke-width "2"}
                 [:circle {:cx "12" :cy "12" :r "10"}]
                 [:path {:d "M9,9h0a3,3,0,0,1,6,0c0,2-3,3-3,3"}]
                 [:path {:d "M12,17h0"}]])]
       (when show-info?
         [:div.absolute.z-10.bg-white.border.border-blue-500.rounded-lg.p-3.shadow-lg.max-w-xs.text-sm.text-gray-600.mt-2.left-4.right-4 {:class ["sm:left-auto" "sm:right-auto" "sm:ml-[-100px]"]}
          [:div.flex.justify-between.items-start.mb-2
           [:span.font-medium.text-gray-900 "Booking Window"]
           [:button.text-gray-400.ml-2 {:class ["hover:text-gray-600"]
                                        :on {:click [[::comp/toggle-calendar-info]]}}
            [:svg.w-4.h-4 {:viewBox "0 0 24 24" :fill "none" :stroke "currentColor" :stroke-width "2"}
             [:line {:x1 "18" :y1 "6" :x2 "6" :y2 "18"}]
             [:line {:x1 "6" :y1 "6" :x2 "18" :y2 "18"}]]]]
          [:p "You can book appointments up to 2 weeks in advance."]])]]
     [:div.flex.space-x-1
      [:button.rounded.transition-colors {:class ["p-1.5" "hover:bg-gray-100"]}
       [:svg.w-4.h-4.text-gray-600 {:viewBox "0 0 24 24" :fill "none" :stroke "currentColor" :stroke-width "2"}
        [:polyline {:points "15,18 9,12 15,6"}]]]
      [:button.rounded.transition-colors {:class ["p-1.5" "hover:bg-gray-100"]}
       [:svg.w-4.h-4.text-gray-600 {:viewBox "0 0 24 24" :fill "none" :stroke "currentColor" :stroke-width "2"}
        [:polyline {:points "9,18 15,12 9,6"}]]]]]))

(defn days-header []
  [:div.grid.grid-cols-7.mb-1
   (for [day day-initials]
     [:div.py-2.text-center.text-xs.font-medium.text-gray-500
      day])])

(defn day-cell [{:keys [day classes current-month? today? date available?]} state]
  (let [selected? (= (str date) (get-in state [:booking :selected-date]))
        animating? (and (get-in state [:ui :animating-item])
                        (= (str date) (get-in state [:ui :animating-item :id]))
                        (= :date (get-in state [:ui :animating-item :type])))
        has-service? (get-in state [:booking :selected-service])
        has-provider? (get-in state [:booking :selected-provider])
        should-stay-on-calendar? (and has-service? has-provider?)
        date-action (if should-stay-on-calendar?
                      ::comp/select-date
                      ::comp/animate-and-select-date)]
    [:div.w-8.h-8.flex.items-center.justify-center.text-sm.rounded.transition-all.duration-200
     {:class (cond
               animating? (into classes ["ring-2" "ring-blue-500" "scale-110" "transform"])
               selected? (into classes ["ring-2" "ring-blue-500"])
               :else classes)
      :on (when available?
            {:click [[date-action (str date)]]})}
     day]))

(defn calendar-grid [state]
  [:div.grid.grid-cols-7.gap-1
   (for [week current-month-data
         day week]
     (day-cell day state))])

(defn calendar [state]
  [:div.max-w-xs.mx-auto.p-4.bg-white.rounded-lg.shadow-sm.border
   (calendar-header state)
   (days-header)
   (calendar-grid state)])

(defn main [state]
  (let [show-info? (get-in state [:ui :show-calendar-info] false)]
    [:div {:on (when show-info?
                 {:click [[::comp/toggle-calendar-info]]})}
     [:div {:on (when show-info?
                  {:click [[::comp/stop-propagation]]})}
      (calendar state)]
     (comp/time-slot-picker mock/time-slots state)
     (comp/booking-summary mock/services mock/users mock/currencies state)]))