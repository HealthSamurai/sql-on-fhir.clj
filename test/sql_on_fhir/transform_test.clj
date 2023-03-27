(ns sql-on-fhir.transform-test
  (:require [sql-on-fhir.transform :as sut]
            [clojure.test :as t]
            [clj-yaml.core]
            [cheshire.core]
            [matcho.core :as matcho]))


(t/deftest test-transform

  (def cases (clj-yaml.core/parse-string (slurp "test_cases/transform.yaml") {:keywords false}))

  (doseq [[case-name {input "input" output "output"}] cases]
    (println case-name)
    (println :in "\n" (clj-yaml.core/generate-string input))
    (println :out "\n" (clj-yaml.core/generate-string output))
    (println :tr "\n" (clj-yaml.core/generate-string (sut/transform {} input)))
    (matcho/match (sut/transform {} input) output)

    )

  )







