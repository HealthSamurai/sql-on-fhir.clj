(ns sql-on-fhir.transform-test
  (:require [sql-on-fhir.transform :as sut]
            [clojure.test :as t]
            [clj-yaml.core]
            [cheshire.core]
            [matcho.core :as matcho]))


(t/deftest test-transform

  (def cases (clj-yaml.core/parse-string (slurp "test_cases/transform.yaml")))

  (doseq [[case-name {input "input" output "output"}] cases]
    (println case-name)
    (println :in "\n" (clj-yaml.core/generate-string input))
    (println :out "\n" (clj-yaml.core/generate-string output))
    (println :tr "\n" (clj-yaml.core/generate-string (sut/transform {} input)))
    (matcho/match (sut/transform {} input) output)

    )

  (sut/translate-ndjson "test/sql_on_fhir/pt.ndjson" "/tmp/result.ndjson")
  ;; (sut/translate-ndjson "test/sql_on_fhir/pt.ndjson" "test/sql_on_fhir/pt.sof.ndjson")

  (t/is (= (slurp "test/sql_on_fhir/pt.sof.ndjson") (slurp "/tmp/result.ndjson")))

  (matcho/match
   (first (sut/read-ndjson "/tmp/result.ndjson"))
   {:multipleBirthBoolean false,
    :address
    [{:sof_extension
      {:geolocation
       [{:url "http://hl7.org/fhir/StructureDefinition/geolocation",
         :sof_extension
         {:latitude
          [{:url "latitude", :valueDecimal 42.419087882595335, :sof_index 0}],
          :longitude
          [{:url "longitude", :valueDecimal -71.149086039334, :sof_index 1}]},
         :sof_index 0}]},
      :line ["785 Gerlach Boulevard Suite 42"],
      :city "Arlington",
      :state "MA",
      :postalCode "02476",
      :country "US"}],
    :meta
    {:profile
     ["http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient"]},
    :name
    [{:use "official",
      :family "Muller251",
      :given ["Rueben647" "Antwan357"],
      :prefix ["Mr."]}],
    :birthDate "1989-12-02",
    :resourceType "Patient",
    :sof_extension
    {:us_core_race
     [{:url "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race",
       :sof_extension
       {:ombCategory
        [{:url "ombCategory",
          :valueCoding
          {:system "urn:oid:2.16.840.1.113883.6.238",
           :code "2106-3",
           :display "White"},
          :sof_index 0}],
        :text [{:url "text", :valueString "White", :sof_index 1}]},
       :sof_index 0}],
     :us_core_ethnicity
     [{:url "http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity",
       :sof_extension
       {:ombCategory
        [{:url "ombCategory",
          :valueCoding
          {:system "urn:oid:2.16.840.1.113883.6.238",
           :code "2186-5",
           :display "Not Hispanic or Latino"},
          :sof_index 0}],
        :text
        [{:url "text", :valueString "Not Hispanic or Latino", :sof_index 1}]},
       :sof_index 1}],
     :patient_mothersMaidenName
     [{:url "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName",
       :valueString "Georgann131 Fritsch593",
       :sof_index 2}],
     :us_core_birthsex
     [{:url "http://hl7.org/fhir/us/core/StructureDefinition/us-core-birthsex",
       :valueCode "M",
       :sof_index 3}],
     :patient_birthPlace
     [{:url "http://hl7.org/fhir/StructureDefinition/patient-birthPlace",
       :valueAddress {:city "Marshfield", :state "Massachusetts", :country "US"},
       :sof_index 4}],
     :disability_adjusted_life_years
     [{:url
       "http://synthetichealth.github.io/synthea/disability-adjusted-life-years",
       :valueDecimal 0.0,
       :sof_index 5}],
     :quality_adjusted_life_years
     [{:url
       "http://synthetichealth.github.io/synthea/quality-adjusted-life-years",
       :valueDecimal 33.0,
       :sof_index 6}]},
    :communication
    [{:language
      {:coding [{:system "urn:ietf:bcp:47", :code "en-US", :display "English"}],
       :sof_codes ["urn:ietf:bcp:47|en-US"],
       :text "English"}}],
    :id "b0e2bc9c-11c2-3ccf-a4f9-a93df80344b8",
    :identifier
    [{:system "https://github.com/synthetichealth/synthea",
      :value "b0e2bc9c-11c2-3ccf-a4f9-a93df80344b8"}
     {:type
      {:coding
       [{:system "http://terminology.hl7.org/CodeSystem/v2-0203",
         :code "MR",
         :display "Medical Record Number"}],
       :sof_codes ["http://terminology.hl7.org/CodeSystem/v2-0203|MR"],
       :text "Medical Record Number"},
      :system "http://hospital.smarthealthit.org",
      :value "b0e2bc9c-11c2-3ccf-a4f9-a93df80344b8"}
     {:type
      {:coding
       [{:system "http://terminology.hl7.org/CodeSystem/v2-0203",
         :code "SS",
         :display "Social Security Number"}],
       :sof_codes ["http://terminology.hl7.org/CodeSystem/v2-0203|SS"],
       :text "Social Security Number"},
      :system "http://hl7.org/fhir/sid/us-ssn",
      :value "999-30-4461"}
     {:type
      {:coding
       [{:system "http://terminology.hl7.org/CodeSystem/v2-0203",
         :code "DL",
         :display "Driver's License"}],
       :sof_codes ["http://terminology.hl7.org/CodeSystem/v2-0203|DL"],
       :text "Driver's License"},
      :system "urn:oid:2.16.840.1.113883.4.3.25",
      :value "S99998964"}
     {:type
      {:coding
       [{:system "http://terminology.hl7.org/CodeSystem/v2-0203",
         :code "PPN",
         :display "Passport Number"}],
       :sof_codes ["http://terminology.hl7.org/CodeSystem/v2-0203|PPN"],
       :text "Passport Number"},
      :system
      "http://standardhealthrecord.org/fhir/StructureDefinition/passportNumber",
      :value "X77003476X"}],
    :telecom [{:system "phone", :value "555-743-6201", :use "home"}],
    :gender "male",
    :maritalStatus
    {:coding
     [{:system "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus",
       :code "M",
       :display "M"}],
     :sof_codes ["http://terminology.hl7.org/CodeSystem/v3-MaritalStatus|M"],
     :text "M"},
    :text
    {:status "generated",
     :div
     "<div xmlns=\"http://www.w3.org/1999/xhtml\">Generated by <a href=\"https://github.com/synthetichealth/synthea\">Synthea</a>.Version identifier: master-branch-latest\n .   Person seed: -1563869585931119458  Population seed: 1679004005360</div>"}})

  )







