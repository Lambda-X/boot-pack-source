# boot-pack-source

[](dependency)
```clojure
[replumb/boot-pack-source "0.1.2"] ;; latest release
```
[](/dependency)

Boot task that collects source files `#{.clj .cljs .cljc .js}` and copies them
in `to-dir`.

This is particularly useful for
[self-hosted REPL](https://github.com/Lambda-X/replumb) apps, which requires
(pun intended) them in order to work properly.

## Usage

The simplest way is to include `pack-source` in your task chain.
For instance from the command line:

```
boot cljs sass pack-source -d "packed-src"
```

As usual, `boot pack-source -h` shows you the option summary.

Note that if you don't specify `-d|--deps`, the current `(get-env)` will be
queried and all the dependencies in `build.boot` will be included.

This time in the repl, another example that dumps everything to the `target`
folder:

```
(boot (pack-source :deps #{['org.clojure/clojurescript "1.8.34"]}
                   :exclude #{#"project.clj"
                              #"third_party\/closure\/.*base.js$"
                              #"third_party\/closure\/.*deps.js$"
                              #"org\/clojure\/clojure\/.*$"}
                   :exclusions '#{org.clojure/clojure
                                  org.mozilla/rhino})
      (built-in/target))
```

For more info on why you would need this see the following blog post:
http://lambdax.io/blog/posts/2015-12-21-cljs-replumb-require.html
  
## Contributing

I suggest first of all to open an issue explaining what is missing and why you
think it should be added. If the reason is compelling, run `boot auto-test` and
freely hack away.

## License

Distributed under the Eclipse Public License, the same as Clojure.

Copyright (C) 2016 Andrea Richiardi & Scalac Sp. z o.o.

## LambdaX

Putting (defn) back into programming

We are a Clojure-centric software house.
Functional experts dedicated to Clojure and ClojureScript.

[lambdax.io](http://lambdax.io/?utm_source=scalac_github&utm_campaign=scalac1&utm_medium=web)
