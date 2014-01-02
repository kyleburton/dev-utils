(defproject aws "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dev-dependencies [[swank-clojure "1.4.2"]]
  :local-repo-classpath true
  :dependencies [
    [org.clojure/clojure                  "1.5.1"]
    [org.clojure/data.json                "0.2.2"]
    [org.clojure/tools.cli                "0.2.1"]
    [com.amazonaws/aws-java-sdk           "1.6.10"]
    [com.github.kyleburton/clj-etl-utils  "1.0.79"]
    [http-kit                             "2.1.15"]
  ])
