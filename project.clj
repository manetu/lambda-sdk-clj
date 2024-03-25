(defproject io.github.manetu/lambda-sdk  "0.0.2"
  :description "An SDK for developing Lambda functions for the Manetu Platform in ClojureScript"
  :url "https://github.com/manetu/lambda-sdk-clj"
  :license {:name "Apache License 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"
            :year 2023
            :key "apache-2.0"}
  :min-lein-version "2.9.0"
  :dependencies [[org.clojure/clojure "1.11.2"]
                 [org.clojure/clojurescript "1.11.132"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library]]]
  :source-paths ["src"])
