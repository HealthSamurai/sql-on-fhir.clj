(ns sql-on-fhir.transform
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [cheshire.core])
  (:import (java.io BufferedInputStream BufferedReader BufferedOutputStream BufferedWriter FileInputStream FileOutputStream InputStreamReader OutputStreamWriter)
           (java.net URL)
           (java.nio.charset StandardCharsets)
           (java.util.zip GZIPInputStream GZIPOutputStream)))

(declare transform)
(defn process-extensions [ctx exts]
  (->> exts
       (map-indexed (fn [i e] [i e]))
       (reduce (fn [acc [i {url :url nested :extension :as ext}]]
                 (if url
                   (let [k (str/replace (last (str/split url #"/")) #"[^_a-zA-Z0-9]+" "_")]
                     (update acc k (fn [x] (conj (or x []) (assoc (transform ctx ext) :sof_index i)))))
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

                     :else
                     (cond->
                         (if (or (map? v) (vector? v))
                           (let [v' (transform ctx v)]
                             (if (and (sequential? v') (seq (mapcat :sof_codes v')))
                               (let [codes (->> (mapcat #(get % :sof_codes) v')
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


(defn gzip-reader [file]
  (let [stream (FileInputStream. file)
        bs (BufferedInputStream. stream)
        gz (GZIPInputStream. bs)
        br (BufferedReader. (InputStreamReader. gz StandardCharsets/UTF_8))]
    br))

(defn gzip-writer [file]
  (let [stream (FileOutputStream. file)
        bs (BufferedOutputStream. stream)
        gz (GZIPOutputStream. bs)]
    (BufferedWriter. (OutputStreamWriter. gz StandardCharsets/UTF_8))))

(defn translate-ndjson [input-file output-file]
  (with-open [w (if (str/ends-with? input-file ".gz")
                  (gzip-writer output-file)
                  (io/writer output-file))]
    (with-open [rdr (if (str/ends-with? output-file ".gz")
                      (gzip-reader input-file)
                      (clojure.java.io/reader (io/file input-file)))]
      (doseq [line (line-seq rdr)]
        (.write w (cheshire.core/generate-string (transform {} (cheshire.core/parse-string line keyword))))
        (.write w "\n")))))

(defn read-ndjson [file]
  (with-open [rdr (if (str/ends-with? file ".gz")
                    (gzip-reader file)
                    (clojure.java.io/reader (io/file file)))]
    (->> (line-seq rdr)
         (mapv (fn [x] (cheshire.core/parse-string x keyword))))))

(defn translate-ndjson-directory [input-dir output-dir]
  (let [output-dir (io/file output-dir)]
    (when (.exists output-dir)
      (throw (Exception. (str output-dir " already exists"))))
    (.mkdir output-dir)
    (->> (.listFiles (io/file input-dir))
         (mapv (fn [file]
                 (when (str/ends-with? (.getName file) ".ndjson")
                   (let [output-file (io/file output-dir (.getName file))]
                     (println :create output-file)
                     (time (translate-ndjson file output-file)))))))))

(comment
  (translate-ndjson "fhir/Patient.ndjson.gz" "/tmp/pt.ndjson.gz")

  (read-ndjson "/tmp/pt.ndjson.gz")

  (translate-ndjson-directory "fhir" "fhir.sof")

  )
