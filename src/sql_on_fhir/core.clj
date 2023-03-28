(ns sql-on-fhir.core
  (:require [sql-on-fhir.transform]
            [sql-on-fhir.macrosql]
            [clojure.java.io :as io])
  (:gen-class))

(def cmds
  {"tx" {:desc "  Transform ndjson files.\n  > tx input-ndjson-file output-ndjson-file\n  > tx input-ndjson-dir output-ndjson-dir"
         :fn (fn [in out]
               (when (not (.exists (io/file in))) (throw (Exception. (str in " does not exists"))))
               (when (.exists (io/file out)) (throw (Exception. (str out " already exists"))))
               (if (.isDirectory (io/file in))
                 (sql-on-fhir.transform/translate-ndjson-directory in out)
                 (sql-on-fhir.transform/translate-ndjson in out))
               (println "transform " in  "to" out))}
   "help" {:desc "  Show help"
           :fn (fn [& _]
                 (->> cmds
                      (mapv (fn [[cmd-name cmd]]
                              (println "\n\n" cmd-name ":\n" (:desc cmd))))))}})

(defn -main [& [cmd & args]]
  (let [cmd (or cmd "help")]
    (if-let [cmd (get cmds cmd)]
      (apply (:fn cmd) args)
      (println :main args))))
