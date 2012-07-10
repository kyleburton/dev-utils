(ns aws.util
  (:require
   [clojure.data.json :as json])
  (:use
   [clj-etl-utils.text :only [string->sha1]]
   [clj-etl-utils.lang-utils :only [raise]]))

(defn sha1-args [args]
  (string->sha1 (json/json-str args)))

(defn wrap-disk-cache [params the-fn]
  (let [cache-dir     (:cache-dir params)
        expired?      (:exp-fn    params (fn [f] false))]
    (when (not (.exists (java.io.File. cache-dir)))
      (.mkdirs (java.io.File. cache-dir)))
    (fn [& args]
      (let [k          (sha1-args args)
            cache-file (str cache-dir "/" k ".dat")]
        (if (and (.exists (java.io.File. cache-file)) (not (expired? cache-file)))
          (read-string (slurp cache-file))
          (let [res (apply the-fn args)]
            (with-open [wr (java.io.FileWriter. cache-file)]
              (print-dup res wr))
            res))))))

(defn is-file-older-than-10-min? [f]
  (> (- (.getTime (java.util.Date.)) (.lastModified (java.io.File. f)))
     (* 600 1000)))

(defmacro def-disk-cache [fn-name arg-spec & body]
  `(def ~fn-name
        (wrap-disk-cache
         {:cache-dir
          (format  "%s/%s/%s/%s"
                   (java.lang.System/getenv "HOME")
                   ".etl.fn.cache"
                   (str *ns*)
                   (str '~fn-name))
          :exp-fn  is-file-older-than-10-min?}
         (fn ~arg-spec
           ~@body))))


(comment

  (def-disk-cache some-function [stuff]
    (println (format "Stuff: %s" stuff))
    stuff)

  (is-file-older-than-10-min? "/home/relay/.etl.fn.cache/aws.elb/some-function/2a5fa0f62abfae6e1a55c2c876312cc8a202be6.dat")
  (some-function {:this "that" :otehr [1 2 3 #{:foof :barf}]})

  )