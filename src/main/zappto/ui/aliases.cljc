(ns zappto.ui.aliases
  (:require [replicant.alias :refer [defalias]]))

;; not used yet

;; an example alias, use it in pages.cljc like: [::ui/btn {::ui/loading? true} "click"]
(defalias btn [attrs & children]
  (let [loading? (::loading? attrs)
        attrs (cond-> (assoc attrs :type "button"))
        children (if loading?
                   (into children [[:span.loading.loading-spinner]])
                   children)]
    [:button.btn attrs children]))

(comment
  [::btn {} "click"]
  (replicant.alias/expand-1 [::btn {::loading? true} [:text-6xl "click"]])
  ;; =>
  ;;[:button
  ;; {:zappto.ui.main/loading? true, :type "button", :aria-busy "true", :tabindex "-1", :class #{"btn"}}
  ;; [:span.loading.loading-spinner]
  ;; [:text-6xl "click"]]

  (replicant.string/render
   [::btn {} "click"])
  ;; => "<button type=\"button\" class=\"btn\">click</button>"
  )
(defalias service-card [attrs children]
  (let [[{:keys [id service-name duration currency price details]} currencies state] children
        details-visible? (get-in state [:details-visibility id] false)
        details-text (if details-visible? "Hide details" "Details")
        current-route (get-in state [:current-route :data :name]) ;; TODO move out to common fn
        next-route (if (or (= current-route :route/homepage)
                           (nil? current-route))
                     :route/team
                     :route/calendar)]
    [:div.bg-base-100.shadow-md.mb-3.w-full.rounded-xl.overflow-hidden.hover:shadow-lg
     [:div.px-4.py-4.cursor-pointer
      [:div
       {:on {:click [[::navigate next-route]]}}
       [:h2.text-lg.font-semibold.mb-2 service-name]]

      [:div.text-base
       [:span duration " mins • " [:span.font-medium (str (get currencies currency) price)]]
       (when details
         [:span " • "
          [:a.text-primary.hover:underline.cursor-pointer
           {:on {:click [[::stop-propagation] [::toggle-details id]]}}
           details-text]])]]

     (when (and details details-visible?)
       [:div.px-4.py-2
        [:p details]])]))

(comment
  (replicant.alias/expand-1 [::service-card
                             {}
                             {:id 1 :service-name "Haircur" :duration "45" :currency "$" :details "Some details"} {}]))

(defalias team-member-card [attrs children]
  (let [[{:keys [id name surname img details]} state] children
        current-route (get-in state [:current-route :data :name])
        next-route (if (or (= current-route :route/homepage)
                           (nil? current-route))
                     :route/services
                     :route/calendar)]
    [:div.mx-auto.w-full.mb-3
     [:div.overflow-hidden.rounded-xl.bg-white.shadow-md
      [:div.flex.items-center.p-4.cursor-pointer
       {:on {:click [[::navigate next-route]]}}
       [:div.flex-shrink-0.mr-4
        [:img.h-12.w-12.rounded-full.object-cover
         {:src img
          :alt (str name " " surname)}]]
       [:div.flex-1
        [:div.font-medium.text-black (str name " " surname)]
        [:div.text-sm.text-gray-500 "Barber"]]]
      (when details
        [:details.border.border-transparent.transition-all {:class ["open:bg-gray-100" "open:border-black/10"]}
         [:summary.text-sm.leading-6.font-semibold.text-gray-900.select-none.p-4.cursor-pointer "View details"]
         [:div.mt-1.text-sm.leading-6.text-gray-600.p-4.pt-0
          [:p details]]])]]))

(comment
  (replicant.alias/expand-1 [::team-member-card
                             {}
                             {:id 1 :name "John" :surname "Doe" :img "path/to/img" :details "Some details"} {}]))