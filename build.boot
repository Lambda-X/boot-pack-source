(set-env!
 :source-paths    #{"src"}
 :resource-paths  #{"resources"}
 :dependencies '[[org.clojure/clojure "1.6.0"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]
                 [degree9/boot-semver "1.2.0" :scope "test"]
                 [clj-jgit "0.8.8"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.0-SNAPSHOT")
(bootlaces! +version+)

(task-options! pom {:project 'boot-cljs-src
                    :version (str +version+ "-standalone")
                    :description "Boot task that collects the ClojureScript source each and every REPL app requires"
                    :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})

(ns-unmap 'boot.user 'test)

;; -- My Tasks --------------------------------------------

(deftask deps [])

(deftask set-dev! []
  (set-env! :source-paths #{"test"}
            :dependencies #(into % '[[adzerk/boot-test "1.1.0" :scope "test"]])))

(deftask test
  "Start auto testing mode (dev profile)"
  []
  (set-dev!)
  (require 'adzerk.boot-test)
  (comp (watch)
        (speak)
        ((eval 'adzerk.boot-test/test))))
