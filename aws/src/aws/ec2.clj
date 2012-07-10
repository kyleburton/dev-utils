(ns aws.ec2
  (:import
   [com.amazonaws.services.ec2
    AmazonEC2Client]
   [com.amazonaws.services.ec2.model
    DescribeInstancesRequest])
  (:use
   aws.aws
   [aws.util                 :only [def-disk-cache]]
   [clj-etl-utils.lang-utils :only [raise]]))

(def ec2-client (AmazonEC2Client. aws-credentials))

(def-disk-cache instance-info [instance-id]
  (first
   (mapcat
    #(vec (map bean (:instances (bean %1))))
    (.getReservations
     (.describeInstances
      ec2-client
      (doto
          (DescribeInstancesRequest.)
        (.setInstanceIds
         [instance-id])))))))

(comment

  (instance-info "i-33b2524b")


  )