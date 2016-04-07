(ns replumb.boot-pack-source
  {:boot/export-tasks true}
  (:require [clojure.string :as string]
            [boot.core :as core :refer [boot]]
            [boot.pod :as pod]
            [boot.from.digest :as digest]
            [boot.util :as util]
            [boot.task.built-in :as built-in]))

;; https://github.com/boot-clj/boot/pull/398
(defn filter-tmpfiles
  "Filter TmpFiles that match (a least one in) metadata-preds.

  Metadata-preds is a set of functions which will receive a TmpFile and
  tried not in a specific order. If no predicate in the set returns
  true-y, the file is filtered out from the returned TmpFiles.

  If invert is specified, the behavior is reversed."
  ([tmpfiles metadata-preds]
   (filter-tmpfiles tmpfiles metadata-preds false))
  ([tmpfiles metadata-preds invert?]
   (when (seq metadata-preds)
     ((if-not invert? filter remove) (apply some-fn metadata-preds) tmpfiles))))

(defn normalize-path
  "Adds a / if missing at the end of the path."
  [path]
  (str path (when-not (= "/" (last path)) "/")))

(core/deftask rebase
  "A task for moving fileset from one dir to another"
  [w with-meta KEY  #{kw}  "The set of metadata keys files must have for being rebased."
   p path      PATH str    "The destination path to rebase the filtered fileset into."
   v invert         bool   "Invert the sense of with-meta."]

  (core/with-pre-wrap fileset
    (let [files (filter-tmpfiles (core/ls fileset) with-meta invert)
          paths (map (juxt :path #(str (normalize-path path) (:path %))) files)]
      (core/commit! (reduce (fn [acc [from-path to-path]]
                              (merge acc (core/mv acc from-path to-path))) fileset paths)))))

(core/deftask sift-jar*
  "Custom version of boot.task.built-in/sift :add-jar which accepts
  a (previously resolved) jar file in input."
  [j jar     PATH  str      "The path of the jar file."
   i include MATCH #{regex} "The set of regexes that paths must match."
   v invert        bool     "Invert the sense of matching."]

  (core/with-pre-wrap fileset
    (core/add-cached-resource fileset (digest/md5 jar)
                              (partial pod/unpack-jar jar)
                              :include (when-not invert include)
                              :exclude (when invert include)
                              :mergers pod/standard-jar-mergers)))

(def default-scopes #{"compile"})

(defn correct-scope?
  [dep-map]
  (contains? default-scopes (:scope dep-map)))

(defn clojurescript?
  [dep-map]
  (= 'org.clojure/clojurescript (:project dep-map)))

(defn include-dependency?
  [dep-map]
  (or (clojurescript? dep-map) (correct-scope? dep-map)))

(defn map-as-dep
  "Returns the given dependency vector with :project and :version put at
  index 0 and 1 respectively and modifiers (eg. :scope, :exclusions,
  etc) next."
  [{:keys [project version] :as dep-map}]
  (let [kvs (remove #(or (some #{:project :version} %)
                         (= [:scope "compile"] %)) dep-map)]
    (vec (remove nil? (into [project version] (flatten kvs))))))

(def ^:private inclusion-regex-set
  "Entries matching these Patterns will be included."
  #{#"LICENSE"
    #"epl-v10"
    #".clj$"
    #".cljs$"
    #".cljc$"
    #".js$"})

(def ^:private exclusion-regex-set
  "Entries matching these Patterns will not be included."
  #{#"project.clj"})

(core/deftask pack-source
  "Add the relevant source files from the project dependencies.

  Specifically, this task moves all the clj, cljs, cljc and js files to the
  to-dir folder specified by the user (defaulting to cljs-src) and keeping
  intact the original namespace structure.

  Currently only the \"compile\" scope is taken into consideration and in case the
  dependencies parameter is missing, the task will use (:dependencies (core/get-env))

  For more info on why you would need this see the following blog post:
  - http://blog.scalac.io/2015/12/21/cljs-replumb-require.html"
  [t to-dir DIR str    "The dir to materialize source files into."
   d deps   DEP #{[sym str]} "The dependency vector to pack."]
  (let [dest-dir (or to-dir "cljs-src")
        env (update (core/get-env) :dependencies
                    #(->> (or (vec deps) %)
                          (map util/dep-as-map)
                          (filter include-dependency?)
                          (map map-as-dep)
                          vec))
        jars (->> (pod/resolve-dependencies env)
                  (map :jar))]
    (util/dbug "Including source from the following jars:\n%s\n" (string/join "\n" jars))
    (comp (reduce #(comp %1 (comp (sift-jar* :jar %2 :include inclusion-regex-set)
                                  (built-in/sift :include exclusion-regex-set
                                                 :invert true)))
                  (built-in/sift :add-meta {#".*" ::initial-fileset})
                  jars)
          (rebase :path dest-dir :with-meta #{::initial-fileset} :invert true))))


(comment
  (reset! util/*verbosity* 2)
  (boot (pack-source))

  (def tmp (core/tmp-dir!))
  (def tmp-str (.tmp))
  (def jar "/home/kapitan/.m2/repository/org/mozilla/rhino/1.7R5/rhino-1.7R5.jar")
  (sift-jar :jar jar :include inclusion-regex)

  (def closure-files (boot.core/by-re [#"closure\/goog\/.*.js$"
                                       #"third_party\/closure\/goog\/.*.js$"] files))
  (def base-deps-files (boot.core/not-by-re [#"third_party\/closure\/.*base.js$"
                                             #"third_party\/closure\/.*deps.js$"] closure-files))

  (util/purge-dir! tmp))
