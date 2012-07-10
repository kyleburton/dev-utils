(ns aws.core
  (:require
   [aws.route53 :as route53]
   [aws.elb     :as elb]
   [aws.cf      :as cf]
   [aws.ec2     :as ec2]
   [clojure.pprint :as pp]
   [clojure.data.json :as json])
  (:use
   [clojure.string :only [join]])
  (:gen-class))


(defn route53-ls-zone [request]
  (let [zone-records (filter #(= "CNAME" (:type %1)) (route53/zone-records (get-in request [:route-params :name])))]
    (doseq [zone-rec zone-records]
      (let [resource-name (.replaceAll (:name zone-rec) "\\.$" "")]
        (doseq [resource (:resourceRecords zone-rec)]
          (println (format "%s\t%s" resource-name (:value resource))))))))

(defn route53-ls [request]
  (doseq [zone (route53/hosted-zones)]
    (let [info (bean zone)]
      (println (format "%s\t%s" zone info)))))

(defn route-matches? [route args]
  (loop [[route-part & route-parts] (:pattern route)
         [arg & args] args
         route-params {}]
    (cond
      (and (not route-part) (not arg))
      (assoc route :route-params route-params)

      (or (not route-part)
          (not arg))
      nil

      (and (= String (class route-part))
           (= route-part arg))
      (recur route-parts args route-params)

      (keyword? route-part)
      (recur route-parts args (assoc route-params route-part arg))

      :no-match
      nil)))

(defn elb-ls [request]
  (doseq [elb (elb/load-balancers)]
    (println (join "\t" (map elb [:loadBalancerName :DNSName])))))

(defn elb-ls-elb [request]
  (let [elb-info (elb/load-balancer-for-resource-name (get-in request [:route-params :name]))]
    (pp/pprint elb-info)))

(defn elb-remove-instance [request]
  (let [elb-name    (get-in request [:route-params :name])
        instance-id (get-in request [:route-params :instance])]
    (elb/deregister-instance elb-name instance-id)))

(defn elb-add-instance [request]
  (let [elb-name    (get-in request [:route-params :name])
        instance-id (get-in request [:route-params :instance])]
    (elb/register-instance elb-name instance-id)))

(defn elb-remove-instance-and-wait [request]
  (let [elb-name    (get-in request [:route-params :name])
        instance-id (get-in request [:route-params :instance])]
    (elb/deregister-instance elb-name instance-id)
    (elb/await-instance-removal elb-name instance-id)))

(defn elb-add-instance-and-wait [request]
  (let [elb-name    (get-in request [:route-params :name])
        instance-id (get-in request [:route-params :instance])]
    (elb/register-instance elb-name instance-id)
    (elb/await-instance-addition elb-name instance-id)))

(defn elb-instance-health [request]
  (let [elb-name    (get-in request [:route-params :name])]
    (doseq [instance-info (elb/instance-health elb-name)]
      (println (join "\t" (map instance-info [:instanceId :state]))))))

(defn elb-ls-instances [request]
  (let [elb-info (elb/load-balancer-for-resource-name (get-in request [:route-params :name]))]
    (doseq [info (:instances elb-info)]
      (let [instance-info (ec2/instance-info (:instanceId info))]
       (println (join "\t"
                      (concat
                       (map info [:instanceId])
                       (map instance-info [:publicDnsName :publicIpAddress :privateIpAddress]))))))))

(defn cf-list-stacks [request]
  (doseq [stack (cf/stacks)]
    (println (join "\t" (map stack [:stackName :stackId])))))

(defn cf-list-stack-instances [request]
  (let [stack-name (get-in request [:route-params :name])]
    (doseq [instance (cf/instances stack-name)]
      (println (join "\t" (map instance [:physicalResourceId :logicalResourceId :resourceStatus]))))))

(defn ec2-instance-info [request]
  (let [instance-id (get-in request [:route-params :name])
        info        (ec2/instance-info instance-id)
        attrs       (vec (map info [:instanceId :publicDnsName :publicIpAddress :privateIpAddress]))
        tags        (:tags info)
        attrs       (conj attrs (json/json-str tags))]
    (println (join "\t" attrs))))

(def routing-table
     [{:pattern ["route53" "ls"]                          :handler route53-ls}
      {:pattern ["route53" "ls" :name]                    :handler route53-ls-zone}
      {:pattern ["elb" "ls"]                              :handler elb-ls}
      {:pattern ["elb" "ls" :name]                        :handler elb-ls-elb}
      {:pattern ["elb" :name "remove" :instance]          :handler elb-remove-instance}
      {:pattern ["elb" :name "add"    :instance]          :handler elb-add-instance}
      {:pattern ["elb" :name "remove-and-wait" :instance] :handler elb-remove-instance-and-wait}
      {:pattern ["elb" :name "add-and-wait"    :instance] :handler elb-add-instance-and-wait}
      {:pattern ["elb" :name "health"]                    :handler elb-instance-health}
      {:pattern ["elb" :name "instances"]                 :handler elb-ls-instances}
      {:pattern ["cf" "ls"]                               :handler cf-list-stacks}
      {:pattern ["cf" :name "instances"]                  :handler cf-list-stack-instances}
      {:pattern ["ec2" "ls" :name]                        :handler ec2-instance-info}])

(defn show-routes []
  (doseq [route routing-table]
    (println (join " " (cons "aws" (:pattern route))))))

(defn find-matching-route [args]
  (loop [[route & routes] routing-table]
    (if (not route)
      nil
      (let [match-info (route-matches? route args)]
        (if (not match-info)
          (recur routes)
          match-info)))))

(defn -main [& args]
  (let [matching-route (find-matching-route args)]
    (cond
      matching-route
      ((:handler matching-route) matching-route)
      :no-matching-route
      (do
        (println (format "no matching command for: %s" args))
        (show-routes)))))




