(ns hellocljs.core
  (:require [manetu.lambda :as lambda]))

(defn handle-request [{{:keys [name]} :params}]
  {:status 200
   :body (str "Hello, " name)})

(defn main []
  (lambda/register-handler handle-request)
  (println "Module Initialized"))
