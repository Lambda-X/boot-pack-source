(ns replumb.boot-cljs-src
  {:boot/export-tasks true}
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [boot.filesystem :as fs]
            [boot.git :as git]
            [boot.pod :as pod]
            [boot.file :as file]
            [boot.core :as core :refer [boot]]
            [boot.util :as boot-util]
            [replumb.boot.util :as util]
            [clj-jgit.porcelain :as jgit]
            [clj-jgit.util :As jgit-util]))

(def closure-git-url "https://github.com/google/closure-library.git")
(def closure-base-src-path "/closure")
(def closure-third-party-src-path "/third_party/closure")

(reset! boot-util/*verbosity* 2)

(def ^:private pod-deps
  '[[clj-jgit "0.8.8"]])

(defn pod-new-env
  "Return a deref-able object which yield the new pod"
  [old-env]
  (delay (-> old-env
             (update-in [:dependencies] (fnil into []) pod-deps)
             (pod/make-pod))))

(defn filter-git-files
  "Returns a non-lazy sequence of java.io.File(s) of the non-nil results
  of (f item). The function f must be free of side-effects and accept
  each java.io.File in the repository.

  The 2-arity version retrieves from HEAD whereas the 3-arity accept
  an (commit-ish) id. In case of invalid/not found commit id it returns
  an empty sequence."
  ([f repo-path]
   (filter-git-files f repo-path "HEAD"))
  ([f repo-path ^org.eclipse.jgit.lib.AnyObjectId id]
   (clj-jgit.porcelain/with-repo repo-path
     (if-let [tree (some-> (.getRepository repo) (.resolve id) (.getTree))]
       (let [commit (-> rev-walk
                        (.parseCommit id)
                        (.getTree))
             twalk  (doto (org.eclipse.jgit.treewalk.TreeWalk. (.getRepository repo))
                      (.addTree tree)
                      (.setRecursive true))
             files (->> (loop [go? (.next twalk) files []]
                          (if-not go?
                            files
                            (recur (.next twalk)
                                   (if (f io/file)
                                     (conj files (.getPathString twalk))
                                     files)))))]
         (boot-util/dbug "%d files in the repo\n" (count files))
         (.release twalk))
       (do (boot-util/dbug "Invalid commit id, return an empty sequence\n")
           [])))))

(core/deftask add-closure-library!
  "Clone the input repository and adds the entire folder to the output
   fileset."
  [u uri    URI  str    "The uri to the git repository"
   r remote STR  str    "The git remote to clone (defaults to origin)"
   b branch STR  str    "The git branch to clone (defaults to master)."]

  (let [remote (or remote "origin")
        branch (or branch "master")
        #_prj-name #_(clj-jgit.util/name-from-uri uri)
        tmp (core/tmp-dir!)
        tmp-string (.getAbsolutePath tmp) ;; necessary because passing in a pod
        pod (pod-new-env (core/get-env))]
    (core/with-pre-wrap fs
      (core/empty-dir! tmp)
      (boot-util/dbug "Tmp-dir is %s\n" tmp)
      (boot-util/info "Cloning %s %s %s...\n" uri remote branch)
      (util/git-clone-in-pod @pod uri tmp-string remote branch)

      (boot-util/info "Filtering the files in the repository...\n")


      #_(doseq [p (git/git-files :ref ref)]
          (file/copy-with-lastmod (io/file p) (io/file tmt p)))
      #_(-> fs (core/add-resource tmp) core/commit!)
      fs)))

(comment
  (def remote "origin")
  (def branch "master")
  (def tmp (core/tmp-dir!))
  (def tmp-string (.getAbsolutePath tmp))
  (def uri closure-git-url)

  (def repo (jgit/load-repo tmp-string))
  (def rev-obj (some-> (.getRepository repo)
                       (.resolve "HEAD")))
  (def commit (-> (org.eclipse.jgit.revwalk.RevWalk. (.getRepository repo))
                  (.parseCommit rev-obj)
                  (.getTree)))

  )
