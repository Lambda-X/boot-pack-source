(ns replumb.boot.util
  (:require [boot.pod :as pod]
            [boot.core :as core])
  (:import java.nio.file.Files
           java.nio.file.attribute.FileAttribute))

(defn tmp-dir!
  "Create a temp folder and returns its java.nio.file.Path."
  []
  (.toFile
   (Files/createTempDirectory nil (make-array FileAttribute 0))))

(defn purge-dir!
  [^java.io.File file]
  (core/empty-dir! file)
  (.delete file))

(defn pod-env
  "Analogous to boot.core/get-env but wrapped in a custom pod"
  [pod]
  (boot.pod/with-eval-in pod boot.pod/env))

(defn git-clone-in-pod
  [pod uri local-dir remote branch]
  (pod/with-call-in pod
    (clj-jgit.porcelain/git-clone-full ~uri ~local-dir ~remote ~branch)))
