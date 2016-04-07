# boot-pack-source

[](dependency)
```clojure
[replumb/boot-pack-source "0.1.0-SNAPSHOT"] ;; latest release
```
[](/dependency)

Boot task that collects source files `#{.clj .cljs .cljc .js}` and copies them
in `to-dir`.

This is particularly useful for
[self-hosted](https://en.m.wikipedia.org/wiki/Self-hosting_compiler) REPL apps,
which requires (pun intended) them in order to work properly.

## Usage

The simplest way is to include `pack-source` in your task chain.
For instance from the command line:

```
boot cljs sass pack-source -d "packed-src"
```


## Contributing

I suggest first of all open an issue explaining what is missing and why you
think it should be added.  Then start auto testing with `boot auto-test` and
freely hack away.

## License

Distributed under the Eclipse Public License, the same as Clojure.

Copyright (C) 2016 Andrea Richiardi & Scalac Sp. z o.o.


## LambdaX
Putting (defn) back into programming

We are a Clojure-centric software house.
Functional experts dedicated to Clojure and ClojureScript.

[lambdax.io](http://lambdax.io/?utm_source=scalac_github&utm_campaign=scalac1&utm_medium=web)
