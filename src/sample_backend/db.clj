(ns sample-backend.db
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]))

(defrecord LanguagesDb [data]

  component/Lifecycle

  (start [component]
    (assoc component :data (-> (io/resource "data.edn")
                               slurp
                               edn/read-string
                               atom)))

  (stop [component]
    (assoc component :data nil)))

(defn new-db
  []
  {:db (map->LanguagesDb {})})


(defn find-language-by-id
  [db language-id]
  (->> db
       :data
       deref
       :languages
       (filter #(= language-id (:id %)))
       first))

(defn find-project-by-id
  [db project-id]
  (->> db
       :data
       deref
       :projects
       (filter #(= project-id (:id %)))
       first))

(defn find-type-system-by-id
  [db type-system-id]
  (->> db
       :data
       deref
       :types
       (filter #(= type-system-id (:id %)))
       first))

(defn list-type-system-for-language
  [db language-id]
  (let [type-id (:type_system (find-language-by-id db language-id))]
    (->> db
         :data
         deref
         :types
         (filter #(= type-id (:id %)))
         first)))

(defn list-projects-for-language
  [db language-id]
  (->> db
       :data
       deref
       :projects
       (filter #(= language-id (:language %)))))

(defn list-paradigms-for-language
  [db language-id]
  (let [paradigms (:paradigms (find-language-by-id db language-id))]
    (->> db
         :data
         deref
         :paradigms
         (filter #(contains? paradigms (:id %))))))

(defn list-languages-for-paradigm
  [db paradigm-id]
  (->> db
       :data
       deref
       :languages
       (filter #(-> % :paradigms (contains? paradigm-id)))))


(defn list-language-for-project
  [db project-id]
  (let [language-id (:language (find-project-by-id db project-id))]
    (->> db
         :data
         deref
         :languages
         (filter #(= language-id (:id %)))
         first)))

(defn list-languages-for-type-system
  [db type-system-id]
  (->> db
       :data
       deref
       :languages
       (filter #(= type-system-id (:type_system %)))))


(defn ^:private apply-type-system-name
  [type-systems type-system-id new-name]
  (->> type-systems
       (remove #(= type-system-id (:id %)))
       (cons {:id type-system-id
              :name new-name})))

(defn change-type-system-name
  [db type-system-id new-name]
  (-> db
      :data
      (swap! update :types apply-type-system-name type-system-id new-name)))
