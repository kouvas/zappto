(ns zappto.ui.pages
  (:require [clojure.string :as str]
            [zappto.ui.components :as comp]
            [zappto.ui.calendar :as cal]
            [zappto.ui.icons :as i]
            [zappto.ui.mock :as d]))

(defn get-team [users team]
  (filter (fn [user]
            (some #(= (:id user) %) team))
          users))

(def sections
  [{:section-key :services
    :icon        i/list-checks-icon
    :content     (fn [state] (for [service d/services]
                               (comp/service-card service d/currencies state)))}
   {:section-key :team
    :icon        i/users-three-icon
    :content     (fn [state] (for [team-member (get-team d/users d/team-ids)]
                               (comp/team-member-card team-member state)))}
   {:section-key :calendar
    :icon        i/calendar-dots-icon
    :content     (fn [state]
                   (cal/main state))}
   {:section-key :info
    :icon        i/info-icon
    :content     (fn [state] "Coming soon...")}])

(defn render-section-by-name [sections section-name state]
  (let [section (some #(when (= (:section-key %) section-name) %) sections)]
    (if section
      (let [{:keys [section-key content]} section]
        (comp/section (str/capitalize (name section-key)) (content state)))
      [:div.text-red-500 "Section not found: " (pr-str section-name)])))

(defn top-section-header [state]
  (let [previous-route (get-in state [:navigation :previous-route])
        back-label     (case previous-route
                         :route/services "Services"
                         :route/team "Team"
                         :route/calendar "Calendar"
                         :route/homepage "Home"
                         "Home")]
    [:div.mb-4.flex.items-center
     [:a.btn.btn-sm.btn-circle.btn-outline {:on {:click [[::comp/navigate-back]]}}
      i/arrow-left-icon]
     [:span.text-xl.font-bold.py-2.px-2 back-label]]))

(defn homepage [state sections]
  [:div.min-h-screen.bg-base-200.p-4.pb-24
   {:replicant/on-render [[::run-scroll-observer]]}
   (comp/top-navbar state sections)
   [:h1.text-xl.font-bold.py-6 "Le Barber Shop"]
   (for [{:keys [section-key]} sections]
     (render-section-by-name sections section-key state))])

(defn render-single-section-page [section-name state]
  [:div.min-h-screen.bg-base-200.p-4.pb-24
   (top-section-header state)
   (render-section-by-name sections section-name state)])

(defn services [state]
  (render-single-section-page :services state))

(defn team [state]
  (render-single-section-page :team state))

(defn calendar [state]
  (render-single-section-page :calendar state))

(defn info [state]
  (render-single-section-page :info state))

(defn render-page [state match]
  (let [view-key (get-in match [:data :view])]
    [:div
     (case view-key
       :pages/homepage-view (homepage state sections)
       :pages/services-view (services state)
       :pages/team-view (team state)
       :pages/calendar-view (calendar state)
       :pages/info-view (info state)
       nil)
     (comp/booking-confirmation-modal state)]))
