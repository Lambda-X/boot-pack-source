(set-env!
 :source-paths #{"src"}
 :dependencies '[[spyscope "0.1.5" :scope "test"]
                 [degree9/boot-semver "1.2.4" :scope "test"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.0-SNAPSHOT")
(bootlaces! +version+)

(task-options! pom {:project 'replumb/boot-pack-source
                    :version +version+
                    :description "Boot task that collects and stores Clojure(Script) source files."
                    :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})

(ns-unmap 'boot.user 'test)

;; -- My Tasks --------------------------------------------

(deftask deps [])

(deftask build []
  (comp
   (pom)
   (jar)
   (install)))

(deftask deploy []
  (comp
   (build)
   (push :repo "clojars" :gpg-sign (not (.endsWith +version+ "-SNAPSHOT")))))

(deftask set-dev! []
  (set-env! :source-paths #(conj % "test")
            :dependencies #(into % '[[adzerk/boot-test "1.1.1" :scope "test"]]))
  (require 'adzerk.boot-test))

(deftask test
  "Testing once (dev profile)"
  []
  (set-dev!)
  (comp ((eval 'adzerk.boot-test/test))))

(deftask auto-test
  "Start auto testing mode (dev profile)"
  []
  (set-dev!)
  (comp (watch)
        (speak)
        ((eval 'adzerk.boot-test/test))))
