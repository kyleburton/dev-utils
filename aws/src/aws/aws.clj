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

(def region-map
     {:default     "ec2.us-east-1.amazonaws.com"
      "virginia"   "ec2.us-east-1.amazonaws.com"
      "oregon"     "ec2.us-west-2.amazonaws.com"
      "nocal"      "ec2.us-west-1.amazonaws.com"
      "ireland"    "ec2.eu-west-1.amazonaws.com"
      "singapore"  "ec2.ap-southeast-1.amazonaws.com"
      "sydney"     "ec2.ap-southeast-2.amazonaws.com"
      "tokyo"      "ec2.ap-northeast-1.amazonaws.com"
      "saopaulo"   "ec2.sa-east-1.amazonaws.com"})