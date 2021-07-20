(ns sample-backend.system
  (:require [com.stuartsierra.component :as component]
            [sample-backend.schema :as schema]
            [sample-backend.server :as server]))

(defn new-system
  []
  (merge (component/system-map)
         (server/new-server)
         (schema/new-schema-provider)))

