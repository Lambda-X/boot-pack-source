(ns replumb.boot-pack-source-test
  (:require [clojure.test :refer :all]
            [boot.util :as util]
            [replumb.boot-cljs-src :refer :all]))

(declare test-fileset test-deps)

(deftest tmpfiles-test

  (testing "Testing filter-tmpfiles"
    (is (:metadata-sample2 (first (filter-tmpfiles (vals (:tree test-fileset)) #{(fn [tmpf] (not (:metadata-sample1 tmpf)))}))))
    (is (:metadata-sample1 (first (filter-tmpfiles (vals (:tree test-fileset)) #{:metadata-sample1}))))
    (is (empty? (filter-tmpfiles (:tree test-fileset) #{}))))

  (testing "Testing filter-tmpfiles inverted"
    (is (:metadata-sample1 (first (filter-tmpfiles (vals (:tree test-fileset)) #{(fn [tmpf] (not (:metadata-sample1 tmpf)))} true))))
    (is (:metadata-sample2 (first (filter-tmpfiles (vals (:tree test-fileset)) #{:metadata-sample1} true))))
    (is (empty? (filter-tmpfiles (:tree test-fileset) #{} true)))))

(deftest deps-test
  (testing "Testing correct-scope?"
    (is (empty? (filter correct-scope? [])))
    (let [dep-maps (map util/dep-as-map test-deps)]
      (is (every? default-scopes (map :scope (filter correct-scope? dep-maps))))
      (is (every? (complement default-scopes) (map :scope (filter (complement correct-scope?) dep-maps))))))

  (testing "Testing clojurescript?"
    (is (empty? (filter clojurescript? [])))
    (is (some #{'org.clojure/clojurescript} (map :project (filter clojurescript? (map util/dep-as-map test-deps)))))
    (let [hacked-dep-maps (map util/dep-as-map (conj test-deps '[org.clojure/clojurescript "1.8.34" :scope "pizza"]))]
      (is (some #{'org.clojure/clojurescript} (map :project (filter clojurescript? hacked-dep-maps))) "It should completely ignore the :scope")))

  (testing "Testing include-dependency?"
    (is (empty? (filter include-dependency? [])))
    (let [test-dep-maps (into #{} (map util/dep-as-map test-deps))]
      (is (every? test-dep-maps (filter include-dependency? test-dep-maps)))
      (let [scopes (conj default-scopes "pizza")
            hacked-dep-maps (into #{} (map util/dep-as-map (conj test-deps '[org.clojure/clojurescript "1.8.34" :scope "pizza"])))]
        (is (every? scopes (map :scope (filter include-dependency? hacked-dep-maps))) "It should still include clojurescript"))))

  (testing "Testing map-as-dep"
    (is (empty? (map-as-dep {})))
    (let [dep-maps (map util/dep-as-map test-deps)]
      (is (= test-deps (into #{} (map map-as-dep (map util/dep-as-map test-deps)))) "It should round-trip"))))


(def test-deps '#{[org.clojure/clojure "1.7.0"]
                  [org.clojure/clojurescript "1.8.34"]
                  [org.clojure/tools.reader "1.0.0-alpha3" :scope "test"]
                  [com.cognitect/transit-cljs "0.8.220"]})

(def test-fileset
  {:dirs
   #{{:dir :real-dir1-java-io-file
      :user true
      :input true
      :output nil}
     {:dir :real-dir2-java-io-file
      :user true
      :input true
      :output nil}}
   :tree {"relative/foo/bar.clj"
          {:dir :real-dir1-java-io-file
           :bdir :cache-dir1-java-io-file
           :path "relative/foo/bar.clj"
           :id "3ff206d57a914e63a61a10369b01f297.1454093818000"
           :hash "3ff206d57a914e63a61a10369b01f297"
           :time 1454093818000
           :metadata-sample1 true}
          "relative/foo/baz.cljs"
          {:dir :real-dir2-java-io-file
           :bdir :cache-dir2-java-io-file
           :path "relative/foo/bar.clj"
           :id "aa0a2cf7eda07f01bc932dfd21b1bca3.1449358564000"
           :hash "aa0a2cf7eda07f01bc932dfd21b1bca3"
           :time 1449358564000
           :metadata-sample2 "I am metadata"}}
   :blob :blob-java-io-file,
   :scratch :scratch-please-java-io-file})
