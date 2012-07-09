(ns aws.route53
  (:use
   [clj-etl-utils.lang-utils :only [raise]]
   [clj-etl-utils.cache-utils :only [def-simple-cached]]
   aws.aws)
  (:import
   [com.amazonaws.services.route53 AmazonRoute53Client]
   [com.amazonaws.services.route53.model GetHostedZoneRequest ListResourceRecordSetsRequest]))

(def-simple-cached hosted-zones []
  (let [route53 (AmazonRoute53Client. aws-credentials)]
    (vec (.getHostedZones (.listHostedZones route53)))))

;; (hosted-zones)

(defn zone-for-name [zone-name]
  (let [zone-info (first (filter
                          (fn [zone]
                            (or
                             (= zone-name (:name zone))
                             (= (str zone-name ".") (:name zone))))
                          (map bean (hosted-zones))))]
    zone-info))

(defn zone-to-id [zone-name]
  (:id (zone-for-name zone-name)))

(def-simple-cached zone-records [zone-name]
  (let [route53 (AmazonRoute53Client. aws-credentials)]
    (vec (map bean (.getResourceRecordSets (.listResourceRecordSets route53 (ListResourceRecordSetsRequest. (zone-to-id zone-name))))))))

(defn all-resource-records []
  (mapcat
   (fn [z] (zone-records (.getName z)))
   (hosted-zones)))

;; (all-resource-records)

(defn records-for-resource [resource-name]
  (filter #(and
            (= "CNAME" (:type %1))
            (= (str resource-name ".") (:name %1)))
          (all-resource-records)))


