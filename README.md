# Rawrepo Dump tool
Rawrepo dump tool is a command line tool for dumping all records for one or more agencies in the rawrepo database.

## Installation
```bash
$ curl -sL http://mavenrepo.dbc.dk/content/repositories/releases/dk/dbc/rawrepo-dump-tool/1.0.0/rawrepo-dump-tool-1.0.0.jar -o rrdump.jar && unzip -op rrdump.jar rrdump.sh | bash -s -- --install
```

Keep the installation up-to-date using the selfupdate action
```bash
rrdump --selfupdate
```

## Usage
````bash
usage: rawrepo-dump-tool [-h] -a AGENCY_ID [AGENCY_ID ...] [-s {ACTIVE,ALL,DELETED}] [-f {LINE,XML,JSON,ISO,LINE_XML}] [-e ENCODING] [-t TYPE [TYPE ...]] [-cf CREATED_FROM] [-ct CREATED_TO] [-mf MODIFIED_FROM] [-mt MODIFIED_TO] -u URL -o FILE [--dryrun [{true,false}]]

Dumps one or more libraries from rawrepo.
Support output in multiple formats and encodings.

optional arguments:
  -h, --help             show this help message and exit
  -a AGENCY_ID [AGENCY_ID ...], --agencies AGENCY_ID [AGENCY_ID ...]
                         List of agencies to dump.
                         E.g. -a 870970 870971 870979
  -s {ACTIVE,ALL,DELETED}, --status {ACTIVE,ALL,DELETED}
                         Status of the records to dump.
                         ACTIVE = active records only, 
                         ALL = both active and deleted records, 
                         DELETED = only deleted records.
                         Defaults to ACTIVE
  -f {LINE,XML,JSON,ISO,LINE_XML}, --format {LINE,XML,JSON,ISO,LINE_XML}
                         Output format.
                         Defaults to LINE. 
                         XML outputs a marcxchange collection
  -e ENCODING, --encoding ENCODING
                         Output character set.
                         eg. LATIN1, UTF-8, and more.
                         Defaults to UTF-8.
  -t TYPE [TYPE ...], --type TYPE [TYPE ...]
                         (Only relevant for FBS agencies) List of record type of FBS records.
                         LOCAL = local records owned by the agency, 
                         ENRICHMENT = enrichments for the agency, 
                         HOLDINGS = records which the agency has holdings on.
                         Mandatory when dumping FBS agency, otherwise ignored. 
                         Note: It might not be possible to dump both rawrepo and holdings for a FBS agency in the same operation due to a known error in the rawrepo-record-service.
  -cf CREATED_FROM, --created-from CREATED_FROM
                         Earliest database creation date (optional). 
                         Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.
                         E.g. 2019-03-06 10:12:42
  -ct CREATED_TO, --created-to CREATED_TO
                         Latest database creation date (optional). 
                         Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.
                         E.g. 2019-03-06 10:12:42
  -mf MODIFIED_FROM, --modified-from MODIFIED_FROM
                         Earliest database modification date (optional). 
                         Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.
                         E.g. 2019-03-06 10:12:42
  -mt MODIFIED_TO, --modified-to MODIFIED_TO
                         Latest database modification date (optional). 
                         Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.
                         E.g. 2019-03-06 10:12:42
  -u URL, --url URL      The URL of the record service.
                         E.g. http://rawrepo-record-service.datawell.cloud.svc.dbc.dk
  -o FILE, --file FILE   The name which the dump should be written to 
                         E.g. 870970.lin
  --dryrun [{true,false}]
                         Dryrun is used for getting the amount of records that will be exported on a normal run.

````
Examples

```bash
rrdump -a 710100 -e UTF-8 -f LINE -t HOLDINGS -u  http://rawrepo-record-service.fbstest.svc.cloud.dbc.dk -o 710100_holdings.lin
```
```bash
rrdump -a 870970 http://rawrepo-record-service.fbstest.svc.cloud.dbc.dk 870970.lin
```
```bash
rrdump -a 870979 -f JSON http://rawrepo-record-service.fbstest.svc.cloud.dbc.dk 870979.json
```
