(ns zappto.ui.mock)

;; duration always in minutes
(def services
  [{:id           1
    :service-name "Men's Regular Cut"
    :duration     30
    :price        40
    :currency     :usd
    :details      "This is a very good service"}
   {:id           2
    :service-name "Buzz Cut"
    :duration     30
    :price        35
    :currency     :usd
    :details      "This is a very good service as well, trust me"}
   {:id           3
    :service-name "Layered Cut"
    :duration     30
    :price        45
    :currency     :usd
    :details      "The best cut ever"}
   {:id           4
    :service-name "Wash, Cut and Style"
    :duration     30
    :price        55
    :currency     :usd
    :details      "You get what you pay for"}
   {:id           5
    :service-name "Shave"
    :duration     30
    :price        40
    :currency     :usd
    ;;:details      "Hey you need a shave?"
    }
   {:id           6
    :service-name "Haircut+Shave"
    :duration     60
    :price        70
    :currency     :usd
    :details      "You wish to get a haircut and a shave?"}])

(def currencies
  {:usd "$"
   :eur "€"})

(def users
  [{:id               10
    :name             "Jeff"
    :surname          "Jefferson"
    :details          "Jeff is solid!"
    :img              "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=facearea&facepad=4&w=256&h=256&q=80"
    :services-offered [1 2 3 4 5 6]}
   {:id               11
    :name             "Jenny"
    :surname          "Jonathan"
    :details          "Da best!"
    :img              "https://tailwindcss.com/_next/static/media/erin-lindford.90b9d461.jpg"
    :services-offered [1 2 3 4]}
   {:id               9
    :name             "Jackie"
    :surname          "Ripper"
    :img              "https://images.unsplash.com/photo-1605405748313-a416a1b84491?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=facearea&facepad=4&w=256&h=256&q=80"
    :details          "Best of the bestest!"
    :services-offered [1 2 3]}])

(def team-ids
  [9 10 11])

(def time-slots
  ["9:00 AM" "9:30 AM" "10:00 AM" "10:30 AM" "11:00 AM" "11:30 AM"
   "1:00 PM" "1:30 PM" "2:00 PM" "2:30 PM" "3:00 PM" "3:30 PM" "4:00 PM"])
