(ns aws.route53
  (:use
   [clj-etl-utils.lang-utils :only [raise rec-bean]]
   aws.aws
   [aws.util                 :only [def-disk-cache]])
  (:import
   [com.amazonaws.services.route53 AmazonRoute53Client]
   [com.amazonaws.services.route53.model GetHostedZoneRequest ListResourceRecordSetsRequest RRType ResourceRecordSet Change ChangeBatch ChangeResourceRecordSetsRequest ChangeAction ResourceRecord]))

(def route53-client (AmazonRoute53Client. aws-credentials))

(comment
  ;;(ChangeBatch.
  ;; awsclient/changeResourceRecordSets
  ;; takes a changeResourceRecordSets
  ;;     take a ChangeBatch
  ;;      takes a list of Changes
  ;;        takes ChangeAction and ResourceRecordSet

  #_(-> route53-client
        (.changeResourceRecordSets
         (ChangeResourceRecordSetsRequest. "HOSTED ZONE ID"
                                           (ChangeBatch. [(Change. ChangeAction/CREATE
                                                                   (-> (ResourceRecordSet. "subdomain.example.com" RRType/CNAME)
                                                                       (.withResourceRecords [(ResourceRecord. "example.com")])
                                                                       (.withTTL 3600)))]))))
  )

(def-disk-cache hosted-zones []
  (vec (map rec-bean (.getHostedZones (.listHostedZones route53-client)))))

;; (hosted-zones)

(defn zone-for-name [zone-name]
  (let [zone-info (first (filter
                          (fn [zone]
                            (or
                             (= zone-name (:name zone))
                             (= (str zone-name ".") (:name zone))))
                          (hosted-zones)))]
    zone-info))

;; (zone-for-name "ec2.relayzone.com")

(defn zone-to-id [zone-name]
  (:id (zone-for-name zone-name)))

;; (zone-to-id "ec2.relayzone.com")

(def-disk-cache zone-records [zone-name]
  (let [req (.listResourceRecordSets route53-client (ListResourceRecordSetsRequest. (zone-to-id zone-name)))]
    (loop [acc (vec (map rec-bean (.getResourceRecordSets req)))
           req req]
      (if (.isTruncated req)
        (let [nreq (.listResourceRecordSets route53-client
                                            (doto
                                                (ListResourceRecordSetsRequest. (zone-to-id zone-name))
                                              (.withStartRecordName (.getNextRecordName req))))]
          (recur
           (vec (concat acc (vec (map rec-bean (.getResourceRecordSets nreq)))))
           nreq))
        acc))))

;; (zone-records "ec2.relayzone.com")

;; (bean (first (:resourceRecords (bean (first (vec (.getResourceRecordSets (.listResourceRecordSets route53-client (ListResourceRecordSetsRequest. (zone-to-id "ec2.relayzone.com"))))))))))

(defn all-resource-records []
  (mapcat
   (fn [z] (zone-records (:name z)))
   (hosted-zones)))

;; (all-resource-records)

(defn records-for-resource [resource-name]
  (filter #(and
            (= "CNAME" (:type %1))
            (or
             (= (str resource-name ".") (:name %1))
             (= resource-name           (:name %1))))
          (all-resource-records)))

;; (records-for-resource "boom-prod-cai.ec2.relayzone.com.")
;; (records-for-resource "boom-prod.ec2.relayzone.com.")


