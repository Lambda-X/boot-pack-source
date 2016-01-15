(ns replumb.boot-cljs-src-test
  (:require [clojure.test :refer :all]
            [replumb.boot.util :as util]
            [replumb.boot-cljs-src :refer :all]))

(def uri "https://github.com/ScalaConsultants/replumb.git")
(def remote "origin")
(def branch "master")

(def ^:dynamic *repo-dir* nil)

(defn clone-repo [f]
  (let [tmp (util/tmp-dir!)
        tmp-string (.getAbsolutePath tmp)
        pod (pod-new-env boot.pod/env)]
    ;; (println "Pod env" (util/pod-env @pod))
    (println "Cloning the repo in" tmp-string)
    (util/git-clone-in-pod @pod uri tmp-string remote branch)
    (binding [*repo-dir* tmp-string]
      (f))
    (util/purge-dir! tmp)))

(use-fixtures :once clone-repo)

(deftest repo-test
  (testing "clone is valid repo"
    (is (clj-jgit.porcelain/load-repo *repo-dir*)) "Should not throw FileNotFoundException and return a git repo")

  (testing "git-files"
    (is (thrown? java.io.FileNotFoundException (git-files "asd")) "Throws when repo is invalid")
    (is (= 1 (count (filter #(and (.exists %) (= "README.md" (.getName %))) (git-files *repo-dir*)))) "There should be one README.md")
    (is (= (git-files *repo-dir*) (git-files *repo-dir*)) "The function git-files is read-only")
    (is (sequential? (git-files *repo-dir* "foo")) "Invalid id input results in a sequence")
    (is (empty (git-files *repo-dir* "foo")) "Invalid id input results in an empty sequence")))
