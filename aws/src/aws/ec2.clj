(ns aws.ec2
  (:import
   [com.amazonaws.services.ec2
    AmazonEC2Client]
   [com.amazonaws.services.ec2.model
    DescribeInstancesRequest])
  (:use
   aws.aws
   [aws.util                 :only [def-disk-cache]]
   [clj-etl-utils.lang-utils :only [raise rec-bean aprog1]]))

(def ec2-client
     (aprog1
         (AmazonEC2Client. aws-credentials)
       (when-let [region (System/getenv "REGION")]
         (.setEndpoint it (get region-map region)))))

(def-disk-cache instance-info [instance-id]
  (first
   (mapcat
    #(vec (map rec-bean (:instances (bean %1))))
    (.getReservations
     (.describeInstances
      ec2-client
      (doto
          (DescribeInstancesRequest.)
        (.setInstanceIds
         [instance-id])))))))

(def-disk-cache all-instances []
  (vec
   (mapcat
    #(vec (map rec-bean (.getInstances %1))) (.getReservations (.describeInstances ec2-client)))))

