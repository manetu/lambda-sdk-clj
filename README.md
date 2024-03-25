# lambda-sdk-clj

[![Clojars Project](https://img.shields.io/clojars/v/io.github.manetu/lambda-sdk.svg)](https://clojars.org/io.github.manetu/lambda-sdk)

This repository hosts an SDK for developing Lambda functions for the Manetu Platform in the [ClojureScript](https://clojurescript.org/) programming language.

## Prerequisites

- [make](https://www.gnu.org/software/make/)
- [JDK](https://jdk.java.net/)
- [Leiningen](https://leiningen.org/)
- [shadow-cljs](https://github.com/thheller/shadow-cljs)
- [mjsc](https://github.com/manetu/javascript-lambda-compiler)
- [wasm-to-oci](https://github.com/engineerd/wasm-to-oci)

## Project setup

The Manetu platform serves Lambda functions within a [WebAssembly (WASM)](https://webassembly.org/) environment.  We can leverage the Clojurescript language in our Lambda functions using a combination of two tools: [shadow-cljs](https://github.com/thheller/shadow-cljs) to compile Clojurescript to an [ECMAScript Module (ESM)](https://tc39.es/ecma262/#sec-modules), followed by [mjsc](https://github.com/manetu/javascript-lambda-compiler) to compile ESM to WASM.

### Create a directory for your project

``` shell
mkdir my-lambda
cd my-lambda
```

### Create the build files

#### shadow-cljs.edn

``` yaml
{:builds {:lambda  {:target     :esm
                    :output-dir "target"
                    :runtime    :custom
                    :modules    {:lambda {:init-fn hellocljs.core/main}}}}
 :lein true}
```

#### project.clj

``` yaml
(defproject hellocljs "0.0.1-SNAPSHOT"
  :min-lein-version "2.9.0"
  :dependencies [[org.clojure/clojure "1.11.2"]
                 [org.clojure/clojurescript "1.11.132"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library]]
                 [io.github.manetu/lambda-sdk "0.0.2"]
                 [thheller/shadow-cljs "2.27.1"]]
  :source-paths ["src"])
```

#### Makefile

``` Makefile
OBJECT=target/lambda.wasm
SRCS = $(shell find src -type f)

all: $(OBJECT)

target:
	mkdir target

target/lambda.js: Makefile project.clj shadow-cljs.edn $(SRCS)
	shadow-cljs release lambda

$(OBJECT): target/lambda.js
	mjsc compile $^ -o $@

clean:
	-rm -rf target
```

### Create the Lambda source

#### Create the source path

``` shell
mkdir -p src/hellocljs
pushd src/hellocljs
```

#### core.cljs

``` clojure
(ns hellocljs.core
  (:require [manetu.lambda :as lambda]))

(defn handle-request [{{:keys [name]} :params}]
  {:status 200
   :body (str "Hello, " name)})

(defn main []
  (lambda/register-handler handle-request)
  (println "Module Initialized"))
```

#### Return to the top-level directory

``` shell
popd
```

### Compile the program

``` shell
make
```

You should now have a file 'target/lambda.wasm' ready for deployment.

### Publish the WASM code

We can leverage any [OCI](https://opencontainers.org/) registry to publish our Lambda function using the [wasm-to-oci](https://github.com/engineerd/wasm-to-oci) tool.

``` shell
$ wasm-to-oci push target/lambda.wasm my-registry.example.com/my-lambda:v0.0.1
INFO[0003] Pushed: my-registry.example.com/my-lambda:v0.0.1
INFO[0003] Size: 1242738
INFO[0003] Digest: sha256:cf9040f3bcd0e84232013ada2b3af02fe3799859480046b88cdd03b59987f3c9
```

### Define a specification for your Lambda function

Create a file 'site.yml' with the following contents:

``` yaml
api-version: lambda.manetu.io/v1alpha1
kind: Site
metadata:
  name: hello
spec:
  runtime: wasi.1.alpha2
  image: oci://my-registry.example.com/my-lambda:v0.0.1
  env:
    LOG_LEVEL: trace
  triggers:
    http-queries:
      - route: /greet
        summary: "Returns a greeting to the user"
        description: "This request allows you to test the ability to deploy and invoke a simple lambda function."
        query-parameters:
          - name: "name"
            schema: { type: "string" }
            description: "The caller's name"
        responses:
          200:
            description: "computed greeting"
            content:
              text/plain:
                schema:
                  type: string
```

Be sure to adjust the image OCI URL.
