(ns manetu.lambda
  "Methods and utilities for Manetu Lambda Functions implemented in Clojure(script)")

(defn js->
  "A utility for converting Javascript objects to Clojure maps"
  [x]
  (js->clj x :keywordize-keys true))

(defn json->
  "A utility for parsing a JSON encoded string to a Clojure data structure"
  [x]
  (-> x
      (js/JSON.parse)
      (js->)))

(defn ->js
  "A utility for converting Clojure maps to a Javascript object"
  [x]
  (clj->js x))

(defn query
  "Invokes a SPARQL-HTTP query to the Manetu backend (See https://www.w3.org/TR/sparql11-protocol/)"
  [expr]
  (let [{:keys [status body] :as r} (js-> (js/lambda.query expr))]
    (if (= status 200)
      (json-> body)
      (throw (ex-info (str "error:" body) r)))))

(defn register-handler
  "Registers a trigger handler function with the Manetu platform"
  [f]
  (js/lambda.register
   (fn [req]
     (try
       (-> req
           (js->)
           (f)
           (->js))
       (catch js/Error ex
         (println "error:" ex)
         (->js {:status 500 :body (ex-message ex)}))))))
