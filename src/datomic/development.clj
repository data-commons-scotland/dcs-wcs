(ns development
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.repl :refer [doc source]]
    [clojure.tools.namespace.repl :as tools-ns :refer [disable-reload! refresh clear set-refresh-dirs]]
    [wcs.components.datomic :refer [datomic-connections]]
    [wcs.components.ring-middleware]
    [wcs.components.server]
    [wcs.model.seed :as seed]
    [wcs.model.account :as account]
    [wcs.model.address :as address]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [com.fulcrologic.rad.database-adapters.datomic :as datomic]
    [com.fulcrologic.rad.resolvers :as res]
    [mount.core :as mount]
    [taoensso.timbre :as log]
    [datomic.api :as d]
    [com.fulcrologic.rad.attributes :as attr]
    [com.fulcrologic.rad.type-support.date-time :as dt]))

(set-refresh-dirs "src/main" "src/datomic" "src/dev" "src/shared" "../fulcro-rad-datomic/src/main" "../fulcro-rad/src/main")

(comment
  (let [db (d/db (:main datomic-connections))]
    (d/pull db '[*] [:account/id (new-uuid 100)])))

(defn seed! []
  (dt/set-timezone! "Europe/London")
  (let [connection (:main datomic-connections)
        date-1     (dt/html-datetime-string->inst "2020-01-01T12:00")
        date-2     (dt/html-datetime-string->inst "2020-01-05T12:00")
        date-3     (dt/html-datetime-string->inst "2020-02-01T12:00")
        date-4     (dt/html-datetime-string->inst "2020-03-10T12:00")
        date-5     (dt/html-datetime-string->inst "2020-03-21T12:00")]
    (when connection
      (log/info "SEEDING data.")
      @(d/transact connection [(seed/new-address (new-uuid 1) "Stir Biz Park")
                               (seed/new-account (new-uuid 100) "Ash" "ash@waste-commons.scot" "openup"
                                 :account/addresses ["Stir Biz Park"]
                                 :account/primary-address (seed/new-address (new-uuid 300) "Stir Uni")
                                 :time-zone/zone-id :time-zone.zone-id/Europe-London)
                               (seed/new-account (new-uuid 101) "Greg" "greg@ewaste-commons" "openup")
                               (seed/new-account (new-uuid 102) "Anna" "anna@ewaste-commons" "openup")
                               (seed/new-account (new-uuid 103) "Hannah" "hannah@waste-commons" "openup")
                               (seed/new-category (new-uuid 1000) "Tools")
                               (seed/new-category (new-uuid 1002) "Toys")
                               (seed/new-category (new-uuid 1003) "Misc")
                               (seed/new-item (new-uuid 200) "Widget" 33.99
                                 :item/category "Misc")
                               (seed/new-item (new-uuid 201) "Screwdriver" 4.99
                                 :item/category "Tools")
                               (seed/new-item (new-uuid 202) "Wrench" 14.99
                                 :item/category "Tools")
                               (seed/new-item (new-uuid 203) "Hammer" 14.99
                                 :item/category "Tools")
                               (seed/new-item (new-uuid 204) "Doll" 4.99
                                 :item/category "Toys")
                               (seed/new-item (new-uuid 205) "Robot" 94.99
                                 :item/category "Toys")
                               (seed/new-item (new-uuid 206) "Building Blocks" 24.99
                                 :item/category "Toys")
                               (seed/new-invoice "invoice-1" date-1 "Ash"
                                 [(seed/new-line-item "Doll" 1 5.0M)
                                  (seed/new-line-item "Hammer" 1 14.99M)])
                               (seed/new-invoice "invoice-2" date-2 "Anna"
                                 [(seed/new-line-item "Wrench" 1 12.50M)
                                  (seed/new-line-item "Widget" 2 32.0M)])
                               (seed/new-invoice "invoice-3" date-3 "Greg"
                                 [(seed/new-line-item "Wrench" 2 12.50M)
                                  (seed/new-line-item "Hammer" 2 12.50M)])
                               (seed/new-invoice "invoice-4" date-4 "Anna"
                                 [(seed/new-line-item "Robot" 6 89.99M)])
                               (seed/new-invoice "invoice-5" date-5 "Hannah"
                                 [(seed/new-line-item "Building Blocks" 10 20.0M)])]))))

(defn start []
  (mount/start-with-args {:config "config/dev.edn"})
  (seed!)
  :ok)

(defn stop
  "Stop the server."
  []
  (mount/stop))

(def go start)

(defn restart
  "Stop, refresh, and restart the server."
  []
  (stop)
  (tools-ns/refresh :after 'development/start))

(def reset #'restart)

