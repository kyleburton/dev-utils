(ns aws.cf
  (:import
   [com.amazonaws.services.cloudformation AmazonCloudFormationClient]
   [com.amazonaws.services.cloudformation.model ListStackResourcesRequest])
  (:use
   aws.aws
   [clj-etl-utils.cache-utils :only [def-simple-cached]]
   [clj-etl-utils.lang-utils :only [raise]]))



(def cf-client
     (AmazonCloudFormationClient. aws-credentials))

(def-simple-cached stacks []
  (vec (map bean (.getStacks (.describeStacks cf-client)))))

(defn stack-info [stack-name]
  ;; find by: stackName, or stackId
  (let [res nil
        res (or res (first (filter #(= stack-name (:stackName %1)) (stacks))))
        res (or res (first (filter #(= stack-name (:stackId %1)) (stacks))))]
    res))

(def-simple-cached resources [stack-name]
  (let [stack-info (stack-info stack-name)]
    (vec
     (map bean
          (.getStackResourceSummaries
           (.listStackResources
            cf-client
            (doto
                (ListStackResourcesRequest.)
              (.setStackName (:stackName stack-info)))))))))

(defn instances [stack-name]
  (filter #(= "AWS::EC2::Instance" (:resourceType %1))
          (resources stack-name)))
(comment

  (stack-info "boom-prod-web")
  (resources "boom-prod-web")
  (instances "boom-prod-web")

  (stacks)

  )