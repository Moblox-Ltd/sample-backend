(ns sample-backend.schema
  (:require
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.util :as util]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.resolve :refer [resolve-as]]
   [com.stuartsierra.component :as component] 
   [clojure.edn :as edn]
   [sample-backend.db :as db]))

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

(defn change-type-system-name
  [db]
  (fn [_ args _]
    (let [{type-system-id :type_system_id
           name :name} args
          type-system (db/find-type-system-by-id db type-system-id)]
      (cond
        (nil? type-system)
        (resolve-as nil {:message "Type System not found."
                         :status 404})

        :else
        (do
          (db/change-type-system-name db type-system-id name)
          (db/find-type-system-by-id db type-system-id))))))

(defn add-language
  [db]
  (fn [_ args _]
    (let [{name :name
           summary :summary
           type-system :type_system
           paradigms :paradigms} args]
      (cond
        (nil? (db/find-type-system-by-id db type-system))
        (resolve-as nil {:message "Type System not found"
                         :status 404})

        :else
        (do
          (db/add-language db name summary type-system paradigms)
          (db/find-language-by-name db name))))))

(defn resolver-map
  [component]
  (let [db (:db component)]
    {:query/language-by-id (language-by-id db)
     :query/project-by-id (project-by-id db)
     :mutation/change-type-system-name (change-type-system-name db)
     :mutation/add-language (add-language db)
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

