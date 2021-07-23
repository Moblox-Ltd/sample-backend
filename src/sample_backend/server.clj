(ns sample-backend.server
  (:require [com.stuartsierra.component :as component]
            [com.walmartlabs.lacinia.pedestal :as lp]
            [io.pedestal.http :as http]))

(defrecord Server [schema-provider server]

  component/Lifecycle

  (start [component]
    (assoc component :server (-> schema-provider
                                 :schema
                                 (lp/service-map {:graphiql true})
                                 (merge {::http/allowed-origins {:allowed-origins (constantly true)}})
                                 http/create-server
                                 http/start)))
  (stop [component]
    (http/stop server)
    (assoc component :server nil)))

(defn new-server
  []
  {:server (component/using (map->Server {})
                            [:schema-provider])})

