(ns aws.core
  (:require
   [aws.route53 :as route53]
   [aws.elb     :as elb]
   [clojure.pprint :as pp])
  (:use
   [clojure.string :only [join]]
   [clj-etl-utils.cache-utils :only [def-simple-cached]])
  (:gen-class))


(defn route53-ls-zone [request]
  (println (format "route53-ls-zone: request=%s" request))
  (let [zone-records (filter #(= "CNAME" (:type %1)) (route53/zone-records (get-in request [:route-params :name])))]
    (doseq [zone-rec zone-records]
      (let [resource-name (.replaceAll (:name zone-rec) "\\.$" "")]
        (doseq [resource (:resourceRecords zone-rec)]
          (println (format "%s\t%s" resource-name (.getValue resource))))))))

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
    (let [elb (bean elb)]
      (println (join "\t" (map elb [:loadBalancerName :DNSName]))))))

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

(defn elb-instance-health [request]
  (let [elb-name    (get-in request [:route-params :name])]
    (doseq [instance-info (elb/instance-health elb-name)]
      (println (join "\t" (map instance-info [:instanceId :state]))))))

(def routing-table
     [
      {:pattern ["route53" "ls"]        :handler route53-ls}
      {:pattern ["route53" "ls" :name]  :handler route53-ls-zone}
      {:pattern ["elb" "ls"]            :handler elb-ls}
      {:pattern ["elb" "ls" :name]      :handler elb-ls-elb}
      {:pattern ["elb" :name "remove" :instance] :handler elb-remove-instance}
      {:pattern ["elb" :name "add"    :instance] :handler elb-add-instance}
      {:pattern ["elb" :name "health"] :handler elb-instance-health}
      ])


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
      (do
        (println (format "match: %s" matching-route))
        ((:handler matching-route) matching-route))
      :no-matching-route
      (println (format "no matching command for: %s" args)))))




























