# Rawrepo Dump tool
Rawrepo dump tool is a command line tool for dumping all records for one or more agencies in the rawrepo database.

## Installation
```bash
$ curl -sL http://mavenrepo.dbc.dk/content/repositories/releases/dk/dbc/rawrepo-dump-tool/1.0.7/rawrepo-dump-tool-1.0.7.jar -o rrdump.jar && unzip -op rrdump.jar rrdump.sh | bash -s -- --install
$ source ~/.bashrc # Or log out and into a new session
```

Keep the installation up-to-date using the selfupdate action
```bash
rrdump --selfupdate
```

## Usage
````bash
usage: rrdump [-h] [-m {RAW,MERGED,EXPANDED}] [-f {LINE,XML,JSON,ISO,LINE_XML}] [-e ENCODING] [-s {ACTIVE,ALL,DELETED}] [-t TYPE [TYPE ...]] [-cf CREATED_FROM] [-ct CREATED_TO] [-mf MODIFIED_FROM] [-mt MODIFIED_TO] -u URL -o FILE [--dryrun] (--all | -a AGENCY_ID [AGENCY_ID ...] | -r RECORDS)

Dumps one or more libraries from rawrepo.
Support output in multiple formats and encodings. 

For more examples see https://github.com/DBCDK/rawrepo-dump-tool

optional arguments:
  -h, --help             show this help message and exit
  --all                  Indicates every records should be dumped. Caution: Can be very slow 
                         Note that --all | -a AGENCY_ID [AGENCY_ID ...] | -r RECORDS are mutually exclusive. Only one method can be chosen
  -a AGENCY_ID [AGENCY_ID ...], --agencies AGENCY_ID [AGENCY_ID ...]
                         List of agencies to dump.
                         Note that --all | -a AGENCY_ID [AGENCY_ID ...] | -r RECORDS are mutually exclusive. Only one method can be chosen 
                         Usage example: -a 870970 870971 870979
  -r RECORDS, --records RECORDS
                         Name of file containing record ids. Format is line separated bibliographicrecordid:agencyid.
                         Note that --all | -a AGENCY_ID [AGENCY_ID ...] | -r RECORDS are mutually exclusive. Only one method can be chosen 
                         Usage example: -r my_records.txt 
                         
                         Example of file content: 
                         51715098:870970
                         68622840:870979
                         877770486:830380
  -m {RAW,MERGED,EXPANDED}, --mode {RAW,MERGED,EXPANDED}
                         Mode of the records. 
                         Defaults to MERGED. 
                         Be aware that choosing EXPANDED takes at least 10 times longer than MERGED
  -f {LINE,XML,JSON,ISO,LINE_XML}, --format {LINE,XML,JSON,ISO,LINE_XML}
                         Output format.
                         Defaults to LINE. 
                         XML outputs a marcxchange collection. 
                         LINE_XML outputs a marcxchange record per line without collection.
  -e ENCODING, --encoding ENCODING
                         Output character set.
                         eg. LATIN1, UTF-8, and more.
                         Defaults to UTF-8.
  -s {ACTIVE,ALL,DELETED}, --status {ACTIVE,ALL,DELETED}
                         Status of the records to dump.
                         ACTIVE = active records only, 
                         ALL = both active and deleted records, 
                         DELETED = only deleted records.
                         Defaults to ACTIVE
  -t TYPE [TYPE ...], --type TYPE [TYPE ...]
                         (Only relevant for FBS agencies - only applicable in agency mode) List of record type of FBS records.
                         LOCAL = local records owned by the agency, 
                         ENRICHMENT = enrichments for the agency, 
                         HOLDINGS = records which the agency has holdings on.
                         Note that if HOLDINGS is selected the output might include local
                         and enrichment records if there happen to be holdings on them.
                         Mandatory when dumping FBS agency, otherwise ignored. 
  -cf CREATED_FROM, --created-from CREATED_FROM
                         Earliest database creation date (optional). 
                         Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.
                         Only applicable in agency mode 
                         Usage example: -cf 2019-03-06 10:12:42
  -ct CREATED_TO, --created-to CREATED_TO
                         Latest database creation date (optional). 
                         Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.
                         Only applicable in agency mode 
                         Usage example: -ct 2019-03-06 10:12:42
  -mf MODIFIED_FROM, --modified-from MODIFIED_FROM
                         Earliest database modification date (optional). 
                         Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.
                         Only applicable in agency mode 
                         Usage example: -mf 2019-03-06 10:12:42
  -mt MODIFIED_TO, --modified-to MODIFIED_TO
                         Latest database modification date (optional). 
                         Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.
                         Only applicable in agency mode 
                         Usage example: -mt 2019-03-06 10:12:42
  -u URL, --url URL      The URL of the record service.
                         Usage example: -u http://rawrepo-record-service.fbstest.svc.cloud.dbc.dk
  -o FILE, --file FILE   The name which the dump should be written to 
                         Usage example: -o 870970.lin
  --dryrun               Dryrun is used for getting the amount of records that will be exported on a normal run.
````
Examples
- All
```bash
rrdump -all -t LOCAL ENRICHMENT -u http://rawrepo-record-service.fbstest.svc.cloud.dbc.dk -o fbstest-all.txt
```
- Agencies
```bash
rrdump -a 710100 -e UTF-8 -f LINE -t HOLDINGS -u http://rawrepo-record-service.fbstest.svc.cloud.dbc.dk -o 710100_holdings.lin
```
```bash
rrdump -a 870970 -u http://rawrepo-record-service.fbstest.svc.cloud.dbc.dk -o 870970.lin
```
```bash
rrdump -a 870979 -f JSON -u http://rawrepo-record-service.fbstest.svc.cloud.dbc.dk -o 870979.json
```

- Records
```bash
rrdump -r ids.txt -o records.txt -u http://rawrepo-record-service.fbstest.svc.cloud.dbc.dk
```
```bash
rrdump -r ids.txt -f JSON -o records.json -u http://rawrepo-record-service.fbstest.svc.cloud.dbc.dk
```
where ids.txt e.g. contains
```bash 
51715098:870970
68622840:870979
877770486:830380
```
