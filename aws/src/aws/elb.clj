(ns aws.elb
  (:require
   [aws.route53 :as route53])
  (:import
   [com.amazonaws.services.elasticloadbalancing AmazonElasticLoadBalancingClient]
   [com.amazonaws.services.elasticloadbalancing.model
    RegisterInstancesWithLoadBalancerRequest
    DeregisterInstancesFromLoadBalancerRequest
    Instance
    DescribeInstanceHealthRequest])
  (:use
   aws.aws
   [clj-etl-utils.cache-utils :only [def-simple-cached]]
   [clj-etl-utils.lang-utils :only [raise]]))

(def elb-client (AmazonElasticLoadBalancingClient. aws-credentials))

(def-simple-cached load-balancers []
  (vec (.getLoadBalancerDescriptions (.describeLoadBalancers elb-client))))

(defn load-balancer-for-resource-name [resource-name]
  (let [elb-dns-name  (.getValue (first (:resourceRecords (first (route53/records-for-resource resource-name)))))]
    (first
     (filter
      (fn [elb]
        (= elb-dns-name (:DNSName elb)))
      (map bean (load-balancers))))))

(defn deregister-instance [elb-dns-name instance-id]
  (let [elb-info (load-balancer-for-resource-name elb-dns-name)]
    elb-info
    (.deregisterInstancesFromLoadBalancer
     elb-client
     (DeregisterInstancesFromLoadBalancerRequest.
      (:loadBalancerName elb-info)
      [(Instance. instance-id)])))
  (clj-etl-utils.cache-utils/purge-standard-caches)
  true)

(defn register-instance [elb-dns-name instance-id]
  (let [elb-info (load-balancer-for-resource-name elb-dns-name)]
    elb-info
    (.registerInstancesWithLoadBalancer
     elb-client
     (RegisterInstancesWithLoadBalancerRequest.
      (:loadBalancerName elb-info)
      [(Instance. instance-id)])))
  (clj-etl-utils.cache-utils/purge-standard-caches)
  true)

(defn instance-health [elb-dns-name]
  (vec
   (map
    bean
    (.getInstanceStates
     (.describeInstanceHealth
      elb-client
      (DescribeInstanceHealthRequest.
       (:loadBalancerName (load-balancer-for-resource-name elb-dns-name))))))))


(defn await-instance-removal [elb-name instance-id]
  (loop [health (instance-health elb-name)]
    (let [])
    ))

(defn await-instance-addition [elb-name instance-id]
  (loop [health (instance-health elb-name)]
    (let [])
    ))