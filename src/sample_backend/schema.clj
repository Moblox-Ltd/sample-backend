(ns sample-backend.schema
  (:require
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.stuartsierra.component :as component]
   [clojure.edn :as edn]))

(defn resolve-element-by-id
  [element-map context args value]
  (let [{:keys [id]} args]
    (get element-map id)))

(defn resolve-language-to
  [element-map key context args language]
  (->> language
       key
       (map element-map)))

(defn resolve-language-to-single
  [element-map key context args language]
  (->> language
       key
       element-map))

(defn resolve-to-languages
  [languages-map key context args value]
  (let [{:keys [id]} value]
    (->> languages-map
         vals
         (filter #(-> % key (contains? id))))))


(defn entity-map
  [data k]
  (reduce #(assoc  %1 (:id %2) %2)
          {}
          (get data k)))

(defn resolver-map
  [component]
  (let [data (-> (io/resource "data.edn")
                 slurp
                 edn/read-string)
        languages-map (entity-map data :languages)
        paradigms-map (entity-map data :paradigms)
        projects-map (entity-map data :projects)
        type-systems-map (entity-map data :types)]
    {:query/language-by-id (partial resolve-element-by-id languages-map)
     :query/project-by-id (partial resolve-element-by-id projects-map)
     :Language/paradigms (partial resolve-language-to paradigms-map :paradigms)
     :Language/type_system (partial resolve-language-to-single type-systems-map :type_system)
     :Language/projects (partial resolve-language-to projects-map :projects)
     :Paradigm/languages (partial resolve-to-languages languages-map :paradigms)
     :Project/languages (partial resolve-to-languages languages-map :projects)
     :TypeSystem/languages (partial resolve-to-languages languages-map :type_system)}))

(defn load-schema
  [component]
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map component))
      schema/compile))

(defrecord SchemaProvider [schema]

  component/Lifecycle

  (start [component]
    (assoc component :schema (load-schema component)))

  (stop [component]
    (assoc component :schema nil)))

(defn new-schema-provider
  []
  {:schema-provider (map->SchemaProvider {})})

