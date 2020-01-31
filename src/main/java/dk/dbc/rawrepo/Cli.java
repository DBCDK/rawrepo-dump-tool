/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.rawrepo;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;

public class Cli {
    Namespace args;

    Cli(String[] args) throws CliException {
        final ArgumentParser parser = ArgumentParsers.newArgumentParser("rrdump")
                .description("Dumps one or more libraries from rawrepo.\n" +
                        "Support output in multiple formats and encodings. \n\n" +
                        "For more examples see https://github.com/DBCDK/rawrepo-dump-tool");

        MutuallyExclusiveGroup group = parser.addMutuallyExclusiveGroup().required(true);

        group.addArgument("-a", "--agencies")
                .help("List of agencies to dump.\n" +
                        "Note and -r and -a are mutually exclusive. Dump tool works in either record mode or agency mode \n" +
                        "Usage example: -a 870970 870971 870979")
                .nargs("+").metavar("AGENCY_ID");

        group.addArgument("-r", "--records")
                .type(Arguments.fileType())
                .help("Name of file containing record ids. Format is line separated bibliographicrecordid:agencyid.\n" +
                        "Note and -r and -a are mutually exclusive. Dump tool works in either record mode or agency mode \n" +

                        "Usage example: -r my_records.txt \n\n" +
                        "Example of file content: \n" +
                        "51715098:870970\n" +
                        "68622840:870979\n" +
                        "877770486:830380");

        parser.addArgument("-m", "--mode")
                .choices(RecordDumpServiceConnector.AgencyParams.Mode.list())
                .setDefault("MERGED")
                .help("Mode of the records. \n" +
                        "Defaults to MERGED. \n" +
                        "Be aware that choosing EXPANDED takes at least 10 times longer than MERGED");

        parser.addArgument("-f", "--format")
                .choices(RecordDumpServiceConnector.AgencyParams.OutputFormat.list())
                .setDefault("LINE")
                .help("Output format.\n" +
                        "Defaults to LINE. \n" +
                        "XML outputs a marcxchange collection. \n" +
                        "LINE_XML outputs a marcxchange record per line without collection.");

        parser.addArgument("-e", "--encoding")
                .setDefault("UTF-8")
                .help("Output character set.\n" +
                        "eg. LATIN1, UTF-8, and more.\n" +
                        "Defaults to UTF-8.");

        parser.addArgument("-s", "--status")
                .choices(RecordDumpServiceConnector.AgencyParams.RecordStatus.list())
                .setDefault("ACTIVE")
                .help("Status of the records to dump.\n" +
                        "ACTIVE = active records only, \n" +
                        "ALL = both active and deleted records, \n" +
                        "DELETED = only deleted records.\n" +
                        "Defaults to ACTIVE");

        parser.addArgument("-t", "--type")
                .choices(RecordDumpServiceConnector.AgencyParams.RecordType.list())
                .help("(Only relevant for FBS agencies - only applicable in agency mode) List of record type of FBS records.\n" +
                        "LOCAL = local records owned by the agency, \n" +
                        "ENRICHMENT = enrichments for the agency, \n" +
                        "HOLDINGS = records which the agency has holdings on.\n" +
                        "Note that if HOLDINGS is selected the output might include local\n" +
                        "and enrichment records if there happen to be holdings on them.\n" +
                        "Mandatory when dumping FBS agency, otherwise ignored. ")
                .nargs("+").metavar("TYPE");

        parser.addArgument("-cf", "--created-from")
                .help("Earliest database creation date (optional). \n" +
                        "Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.\n" +
                        "Only applicable in agency mode \n" +
                        "Usage example: -cf 2019-03-06 10:12:42");

        parser.addArgument("-ct", "--created-to")
                .help("Latest database creation date (optional). \n" +
                        "Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.\n" +
                        "Only applicable in agency mode \n" +
                        "Usage example: -ct 2019-03-06 10:12:42");

        parser.addArgument("-mf", "--modified-from")
                .help("Earliest database modification date (optional). \n" +
                        "Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.\n" +
                        "Only applicable in agency mode \n" +
                        "Usage example: -mf 2019-03-06 10:12:42");

        parser.addArgument("-mt", "--modified-to")
                .help("Latest database modification date (optional). \n" +
                        "Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.\n" +
                        "Only applicable in agency mode \n" +
                        "Usage example: -mt 2019-03-06 10:12:42");

        parser.addArgument("-u", "--url")
                .required(true)
                .help("The URL of the record service.\n" +
                        "Usage example: -u http://rawrepo-record-service.fbstest.svc.cloud.dbc.dk");

        parser.addArgument("-o", "--file")
                .required(true)
                .type(Arguments.fileType())
                .help("The name which the dump should be written to \n" +
                        "Usage example: -o 870970.lin");

        parser.addArgument("--dryrun")
                .type(Boolean.class)
                .nargs("?")
                .setConst(true)
                .help("Dryrun is used for getting the amount of records that will be exported on a normal run.");

        try {
            this.args = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            throw new CliException(e);
        }
    }
}
