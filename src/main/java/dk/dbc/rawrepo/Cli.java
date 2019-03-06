/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.rawrepo;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Cli {
    Namespace args;

    Cli(String[] args) throws CliException {
        final ArgumentParser parser = ArgumentParsers.newArgumentParser("rawrepo-dump-tool")
                .description("Dumps one or more libraries from rawrepo.\n" +
                        "Support output in multiple formats and encodings.");

        parser.addArgument("-a", "--agencies")
                .help("List of agencies to dump, separated by comma.\n" +
                        "E.g. 870970,870971,870979")
                .nargs("+").metavar("AGENCY_ID");

        parser.addArgument("-s", "--status")
                .choices("ACTIVE", "ADD", "DELETED")
                .setDefault("ACTIVE")
                .help("Status of the records to dump.\n" +
                        "ACTIVE = active records only, ALL = both active and deleted records, DELETED = only deleted records.\n" +
                        "Defaults to ACTIVE");

        parser.addArgument("-f", "--format")
                .choices("LINE", "JSON", "XML")
                .setDefault("LINE")
                .help("Output format.\n" +
                        "Defaults to LINE");

        parser.addArgument("-e", "--encoding")
                .setDefault("UTF-8")
                .help("Output character set.\n" +
                        "eg. LATIN-1, DANMARC2, MARC-8, UTF-8, and more.\n" +
                        "Defaults to UTF-8.");

        parser.addArgument("-t", "--type")
                .choices("LOCAL", "ENRICHMENT", "HOLDINGS")
                .help("(Only relevant for FBS agencies) List of record type of FBS records.\n" +
                        "LOCAL = local records owned by the agency, ENRICHMENT = enrichments for the agency, HOLDINGS = records which the agency has holdings on.\n" +
                        "Mandatory when dumping FBS agency, otherwise ignored");

        parser.addArgument("-cf", "--created-from")
                .help("Earliest creation date (optional). Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.\n" +
                        "E.g. 2019-03-06 10:12:42");

        parser.addArgument("-ct", "--created-to")
                .help("Lastest creation date (optional). Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.\n" +
                        "E.g. 2019-03-06 10:12:42");

        parser.addArgument("-mf", "--modified-from")
                .help("Earliest modification date (optional). Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.\n" +
                        "E.g. 2019-03-06 10:12:42");

        parser.addArgument("-mt", "--modified-to")
                .help("Lastest modification date (optional). Format is either 'YYYY-MM-DD' or 'YYYY-MM-DD HH:mm:ss'.\n" +
                        "E.g. 2019-03-06 10:12:42");

        parser.addArgument("-u", "--url")
                .help("The URL of the record service.\n"+
                "E.g. http://rawrepo-record-service.cloud.svc.dbc.dk");

        try {
            this.args = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            throw new CliException(e);
        }
    }
}
