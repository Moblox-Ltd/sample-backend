(ns sample-backend.core
  (:require
   [sample-backend.system :as system]
   [com.stuartsierra.component :as component]))

(defonce system (system/new-system))


(defn start
  []
  (alter-var-root #'system component/start-system)
  :started)

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating your server...")
  (start))
