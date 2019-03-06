/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.rawrepo;

import java.util.List;

public class DumpApp {

    public static void main(String[] args) {
        try {
            runWith(args);
        } catch (CliException e) {
            System.exit(1);
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    static void runWith(String[] args) throws CliException {
        final Cli cli = new Cli(args);

        List<String> agencies = cli.args.get("agencies");
        String recordStatus = cli.args.get("status");
        String recordType = cli.args.get("type");

        String outputFormat = cli.args.get("format");
        String outputEncoding = cli.args.get("encoding");

        String createdFrom = cli.args.get("created-from");
        String createdTo = cli.args.get("created-to");
        String modifiedFrom = cli.args.get("modified-from");
        String modifiedTo = cli.args.get("modified-to");

    }

}
