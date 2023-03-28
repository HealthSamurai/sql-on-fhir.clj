# sql-on-fhir.clj

Reference implementation of SQL on FHIR in clojure


## Getting started

* Install clojure - https://clojure.org/guides/install_clojure
* Install synthea - https://synthea.mitre.org/ or download pre-generated data

```bash

cd ~/synthea && ./run_synthea --exporter.baseDirectory="<DATADIR>" --exporter.fhir.bulk_data=true -p 100

git clone git@github.com:HealthSamurai/sql-on-fhir.clj.git

cd sql-on-fhir.clj

# run transformation of ndjson file
clj -M -m sql-on-fhir.core tx <DATADIR>/fhir/Patient.ndjson.gz <DATADIR>/sof/Patient.ndjson.gz

# transform folder of ndjson files
clj -M -m sql-on-fhir.core tx <DATADIR>/fhir/ <DATADIR>/sof


```
