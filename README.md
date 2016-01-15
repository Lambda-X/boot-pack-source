# replumb/boot-cljs-src

Boot task that collects the ClojureScript files each and every [self-hosted](https://en.m.wikipedia.org/wiki/Self-hosting_compiler) REPL app requires (pun intended) in order to work properly.

Expanding on the topic a bit, the final [Boot fileset](https://github.com/boot-clj/boot/wiki/Filesets) will include all the source files that `require`, `source` and co. read at runtime. More details [in this blog post](http://blog.scalac.io/2015/12/21/cljs-replumb-require.html).

## Usage

TBD

## Contributing

I suggest first of all to start auto testing with `boot test` and then freerly hack some basic test for your new feature.

## License

Distributed under the Eclipse Public License, the same as Clojure.

Copyright (C) 2016 Scalac Sp. z o.o.

Scalac [scalac.io](http://scalac.io/?utm_source=scalac_github&utm_campaign=scalac1&utm_medium=web) is a full-stack team of great functional developers focused around Scala/Clojure backed by great frontend and mobile developers.

On our [blog](http://blog.scalac.io/?utm_source=scalac_github&utm_campaign=scalac1&utm_medium=web) we share our knowledge with community on how to write great, clean code, how to work remotely and about our culture.
