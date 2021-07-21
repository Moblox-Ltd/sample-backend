(ns sample-backend.schema
  (:require
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.stuartsierra.component :as component]
   [clojure.edn :as edn]
   [sample-backend.db :as db]))

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

(defn language-by-id
  [db]
  (fn [_ args _]
    (db/find-language-by-id db (:id args))))


(defn language-paradigms
  [db]
  (fn [_ _ language]
    (db/list-paradigms-for-language db (:id language))))

(defn language-projects
  [db]
  (fn [_ _ language]
    (db/list-projects-for-language db (:id language))))

(defn language-type-system
  [db]
  (fn [_ _ language]
    (db/list-type-system-for-language db (:id language))))

(defn paradigm-languages
  [db]
  (fn [_ _ paradigm]
    (db/list-languages-for-paradigm db (:id paradigm))))

(defn type-system-languages
  [db]
  (fn [_ _ type-system]
    (db/list-languages-for-type-system db (:id type-system))))

(defn project-language
  [db]
  (fn [_ _ project]
    (db/list-language-for-project db (:id project))))

(defn project-by-id
  [db]
  (fn [_ args _]
    (db/find-project-by-id db (:id args))))

(defn resolver-map
  [component]
  (let [db (:db component)]
    {:query/language-by-id (language-by-id db)
     :query/project-by-id (project-by-id db)
     :Language/paradigms (language-paradigms db)
     :Language/type_system (language-type-system db)
     :Language/projects (language-projects db)
     :Paradigm/languages (paradigm-languages db)
     :Project/languages (project-language db)
     :TypeSystem/languages (type-system-languages db)}))

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
  {:schema-provider (-> {}
                        map->SchemaProvider
                        (component/using [:db]))})

