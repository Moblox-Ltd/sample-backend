(defproject sample-backend "0.1.0-SNAPSHOT"
  :description "Simple backend to use for tests"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.walmartlabs/lacinia-pedestal "0.16.1"]
                 [com.stuartsierra/component "1.0.0"]
                 [io.aviso/logging "1.0"]]
  
  :uberjar-name "sample-backend.jar"
  :main sample-backend.core
  :aot [sample-backend.core]  )
