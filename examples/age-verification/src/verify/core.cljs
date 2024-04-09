(ns verify.core
  (:require [manetu.lambda :as lambda]
            [cljstache.core :refer [render]]
            [cljs-time.core :as t]
            [cljs-time.format :as t.format]))

(def query-template
  "
PREFIX id: <http://example.gov/rmv/>
SELECT ?dob
WHERE {
   ?s id:biometric-hash "{{biometric-hash}}" ;
      id:dob            ?dob .
}
")

(def time-fmt (t.format/formatters :date))

(defn get-dob [bindings]
  (t.format/parse time-fmt (get-in bindings [0 :dob :value])))

(defn verify-age [bindings]
  (let [dob (get-dob bindings)
        minimum (t/minus (t/now) (t/years 21))]
    (t/before? dob minimum)))

(defn handle-request [{:keys [params]}]
  (try
    (let [{{:keys [bindings]} :results} (lambda/query (render query-template params))]
      (case (count bindings)
        0 {:status 200 :body "not-found"}
        1 {:status 200 :body (verify-age bindings)}
        {:status 500 :body "unexpected multiple matching results"}))
    (catch js/Error ex
      {:status 500 :body (str "query error: " (ex-message ex))})))

(defn main []
  (lambda/register-handler handle-request)
  (println "Module Initialized"))
