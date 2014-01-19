(ns aws.glacier
  (:import
   [com.amazonaws.services.ec2 AmazonEC2Client]
   [com.amazonaws.auth AWSCredentials PropertiesCredentials]
   [com.amazonaws.services.glacier AmazonGlacierClient]
   [com.amazonaws.services.glacier.transfer ArchiveTransferManager
    UploadResult]
   [com.amazonaws.services.glacier.model
    ListVaultsRequest
    DescribeVaultRequest
    ListJobsRequest])
  (:require
   [aws.aws                  :refer [aws-credentials]]
   [aws.util                 :refer [def-disk-cache]]
   [clj-etl-utils.lang-utils :refer [rec-bean]]))

(def default-glacier-region "oregon")

(def region-endpoint-map
     {:default     "glacier.us-west-2.amazonaws.com"
      "oregon"     "glacier.us-west-2.amazonaws.com"})

(def glacier-client
     (let [it (AmazonGlacierClient. aws-credentials)]
       (when-let [region (or (System/getenv "REGION") default-glacier-region)]
         (.setEndpoint it (get region-endpoint-map region)))
       it))

(defn with-archive-transfer-manager* [f]
  (let [atm (ArchiveTransferManager. glacier-client aws-credentials)]
    (f atm)))

(defmacro with-archive-transfer-manager [client-name & body]
  `(with-archive-transfer-manager* (fn [~client-name] ~@body)))

(defn upload-archive [vault-name local-file archive-name]
  (with-archive-transfer-manager atm
    (let [result (.upload atm vault-name archive-name
                          (java.io.File. local-file))]
      (.getArchiveId result))))

(defn list-vaults [region]
  (let [vreq (doto
                 (ListVaultsRequest.)
               (.withAccountId "-")
               (.withLimit "10"))]
    (map rec-bean (seq (.getVaultList (.listVaults glacier-client vreq))))))


(defn describe-vault [vault-name]
  (rec-bean (.describeVault glacier-client
                            (doto
                                (DescribeVaultRequest.)
                              (.withAccountId "-")
                              (.withVaultName vault-name)))))

(defn list-jobs [vault-name]
  (map rec-bean
       (seq
        (.getJobList
         (.listJobs
          glacier-client
          (doto
              (ListJobsRequest.)
            (.withAccountId "-")
            (.withVaultName vault-name)
            (.withLimit "10")))))))

(comment
  (describe-vault "LongTermBackup")
  (list-jobs "LongTermBackup")

  (def chicken (list-vaults "oregon"))
  chicken

  (def archive-id
       (upload-archive "LongTermBackup" "/home/relay/confluence.tar.bz2"
                       (format "confluence.tar.bz2:%s" (java.util.Date.))))

  (def archive-id
       (upload-archive "LongTermBackup" "/home/relay/mock-usan.tab"
                       (format "test-archive.20140102:%s" (java.util.Date.))))
  (def archive-id
       (upload-archive "LongTermBackup" "/home/relay/x.x"
                       (format "test-x.x.20140102:%s" (java.util.Date.))))

  archive-id

  )