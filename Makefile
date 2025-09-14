nrepl:
	clojure -M:nrepl

node_modules:
	npm install

# tailwind task depends on node_modules
tailwind: node_modules
	npx tailwindcss -i ./src/main.css -o ./resources/public/styles.css --watch

shadow: node_modules
# must open the web page to have a running repl
	npx shadow-cljs watch app

# The .PHONY directive in a Makefile is used to specify targets that do not represent actual files.
# This tells make to always execute the commands associated with these targets, even if a file or
# directory with the same name exists in the filesystem.
.PHONY: tailwind shadow
