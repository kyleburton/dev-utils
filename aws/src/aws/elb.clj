(ns aws.elb
  (:require
   [aws.route53 :as route53])
  (:import
   [com.amazonaws.services.elasticloadbalancing AmazonElasticLoadBalancingClient])
  (:use
   aws.aws
   [clj-etl-utils.cache-utils :only [def-simple-cached]]
   [clj-etl-utils.lang-utils :only [raise]]))



(def-simple-cached load-balancers []
  (vec (.getLoadBalancerDescriptions (.describeLoadBalancers (AmazonElasticLoadBalancingClient. aws-credentials)))))

(defn load-balancer-for-resource-name [resource-name]
  (let [elb-dns-name  (.getValue (first (:resourceRecords (first (route53/records-for-resource resource-name)))))]
    (first
     (filter
      (fn [elb]
        (= elb-dns-name (:DNSName elb)))
      (map bean (load-balancers))))))

