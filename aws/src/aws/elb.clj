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
   [aws.util :only [def-disk-cache]]
   [clj-etl-utils.lang-utils :only [raise rec-bean]]))

(def elb-client (AmazonElasticLoadBalancingClient. aws-credentials))

(def-disk-cache load-balancers []
  (vec (map rec-bean (.getLoadBalancerDescriptions (.describeLoadBalancers elb-client)))))

;; (first (filter #(= "boom-prod-RelayEla-MICNJ7UKID0H" (:loadBalancerName %1)) (map bean (load-balancers))))
;;(map bean (load-balancers))

;; (load-balancer-for-dns-name "boom-prod.ec2.relayzone.com")
;; (:resourceRecords (first (route53/records-for-resource "boom-prod.ec2.relayzone.com")))

(defn load-balancer-for-dns-name [resource-name]
  (let [elb-info      (first (:resourceRecords (first (route53/records-for-resource resource-name))))
        elb-dns-name  (if elb-info (:value elb-info))]
    (if elb-info
      (first
       (filter
        (fn [elb]
          (= elb-dns-name (:DNSName elb)))
        (load-balancers))))))

;; (load-balancer-for-dns-name "")

(defn load-balancer-for-resource-name [resource-name]
  (or
   (first (filter #(= resource-name (:loadBalancerName %1)) (load-balancers)))
   (load-balancer-for-dns-name resource-name)))

(defn deregister-instance [elb-dns-name instance-id]
  (let [elb-info (load-balancer-for-resource-name elb-dns-name)]
    elb-info
    (.deregisterInstancesFromLoadBalancer
     elb-client
     (DeregisterInstancesFromLoadBalancerRequest.
      (:loadBalancerName elb-info)
      [(Instance. instance-id)])))
  true)

(defn register-instance [elb-dns-name instance-id]
  (let [elb-info (load-balancer-for-resource-name elb-dns-name)]
    elb-info
    (.registerInstancesWithLoadBalancer
     elb-client
     (RegisterInstancesWithLoadBalancerRequest.
      (:loadBalancerName elb-info)
      [(Instance. instance-id)])))
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


;; (instance-health "boom-prod-RelayEla-MICNJ7UKID0H")

(defn await-instance-removal [elb-name instance-id]
  (loop [health (instance-health elb-name)]
    (if (empty? (filter #(= instance-id (:instanceId %1)) health))
      true
      (do
        (println (format "Waiting for %s to be removed from the elb, health checks=%s" health))
        (recur (instance-health elb-name))))))

(defn await-instance-addition [elb-name instance-id]
  (loop [health (instance-health elb-name)]
    (let [entry (first (filter #(= instance-id (:instanceId %1)) health))]
      (cond
        (not entry)
        (do
          (println (format "[%s] Waiting for %s to be added to the elb, health checks=%s" elb-name instance-id health))
          (recur (instance-health elb-name)))
        (= (:state entry) "InService")
        (do
          (println (format "[%s] %s has joined the elb and is in service." elb-name instance-id))
          true)
        :not-yet-in-service
        (do
          (recur (instance-health elb-name)))))))
