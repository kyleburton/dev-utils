(ns aws.aws
  (:use
   [clj-etl-utils.lang-utils :only [raise]]))

(defn aws-credentials-info []
  (let [cred-info (slurp (format "%s/.aws/credentials" (java.lang.System/getenv "HOME")))]
    (reduce
     (fn [m line]
       (let [[k v] (.split line "=")]
         (assoc m k v)))
     {}
     (.split cred-info "\n"))))

(def aws-credentials
     (let [info (aws-credentials-info)]
       (com.amazonaws.auth.BasicAWSCredentials. (get info "AWSAccessKeyId")
                                                (get info "AWSSecretKey"))))

