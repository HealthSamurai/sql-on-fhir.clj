(ns sql-on-fhir.transform
  (:require [clojure.string :as str]))

(declare transform)
(defn process-extensions [ctx exts]
  (->> exts
       (reduce (fn [acc {url :url nested :extension :as ext}]
                 (if url
                   (let [k (keyword (str/replace (last (str/split url #"/")) #"[^_a-zA-Z0-9]+" "_"))]
                     (update acc k (fn [x]
                                     (conj (or x []) (transform ctx ext)))))
                   acc))
               {})))

(defn parse-ref [s]
  (let [parts (reverse (str/split s #"/"))]
    (when (>= (count parts) 2)
      [(second parts) (first parts)])))

(defn transform [ctx obj]
  (cond
    (map? obj)
    (->> obj
         (reduce (fn [acc [k v]]
                   (cond
                     (and (= k :reference) (string? v))
                     (let [[type id] (parse-ref v)]
                       (assoc acc k v :sof_id id :type type))

                     (and (= k :coding) (sequential? v))
                     (assoc acc k (transform ctx v) :sof_codes (->> v (mapv (fn [{s :system c :code}] (str s "|" c)))))

                     (and (= k :component) (sequential? v))
                     (assoc acc k (transform ctx v)
                            :sof_component (group-by (fn [x] (get-in x [:code :coding 0 :code])) v))

                     :else
                     (cond->
                         (if (or (map? v) (vector? v))
                           (let [v' (transform ctx v)]
                             (if (and (sequential? v') (seq (mapcat :sof_codes v')))
                               (let [codes (->> (mapcat :sof_codes v')
                                                (into #{})
                                                (into []))]
                                 (assoc acc (keyword (str "sof_" (name k) "_codes")) codes k v'))
                               (assoc acc k v')))
                           (assoc acc k v))
                       (= k :extension)
                       (-> (dissoc :extension)
                           (assoc :sof_extension (process-extensions ctx v))))))
                 {}))

    (sequential? obj)
    (->> obj
         (reduce (fn [acc v]
                   (conj acc (if (or (map? v) (vector? v)) (transform ctx v) v)))
                 []))
    :else obj))

(defn translate-ndjson [input-file output-file]

  )

(defn translate-ndjson-directory [input-dir output-dir]

  )
