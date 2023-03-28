# sql-on-fhir.clj

Reference implementation of SQL on FHIR in clojure

## Transformation

* reference transformation
* extensions transformation
* codes materialization


## Getting started

* Install clojure - https://clojure.org/guides/install_clojure
* Install synthea - https://synthea.mitre.org/ or download pre-generated data
* Install duckdb - https://duckdb.org/docs/installation/index

```bash

cd ~/synthea && ./run_synthea --exporter.baseDirectory="<DATADIR>" --exporter.fhir.bulk_data=true -p 100

git clone git@github.com:HealthSamurai/sql-on-fhir.clj.git

cd sql-on-fhir.clj

# run transformation of ndjson file
clj -M -m sql-on-fhir.core tx <DATADIR>/fhir/Patient.ndjson.gz <DATADIR>/sof/Patient.ndjson.gz

# transform folder of ndjson files
clj -M -m sql-on-fhir.core tx <DATADIR>/fhir/ <DATADIR>/sof


```

Play with duckdb

```bash

cd <DATADIR>/sof/

duckdb fhir.duckdb

D select * from read_json_auto('Patient.ndjson.gz') limit 10;
D CREATE TABLE patient AS select * from read_json_auto('Patient.ndjson.gz');
D PRAGMA show('patient');
D select gender, sof_extension.us_core_race[1].sof_extension.text[1].valueString, count(*) from patient group by 1,2 limit 10;

# generate parquet

D COPY (SELECT * FROM patient) TO 'Patient.parquet' (FORMAT 'parquet');



```
