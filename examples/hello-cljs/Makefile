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
