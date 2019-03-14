/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.rawrepo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

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

        List<Integer> agencies = cli.args.get("agencies");
        String recordStatus = cli.args.get("status");
        List<String> recordTypes = cli.args.get("type");

        String outputFormat = cli.args.get("format");
        String outputEncoding = cli.args.get("encoding");

        String createdFrom = cli.args.get("created_from");
        String createdTo = cli.args.get("created_to");
        String modifiedFrom = cli.args.get("modified_from");
        String modifiedTo = cli.args.get("modified_to");

        String fileName = cli.args.get("file");
        String url = cli.args.get("url");

        RecordDumpServiceConnector.Params params = new RecordDumpServiceConnector.Params()
                .withAgencies(agencies);

        if (!recordStatus.isEmpty()) {
            params.withRecordStatus(RecordDumpServiceConnector.Params.RecordStatus.valueOf(recordStatus));
        }

        if (recordTypes != null && !recordTypes.isEmpty()) {
            params.withRecordType(recordTypes.stream()
                    .map(RecordDumpServiceConnector.Params.RecordType::valueOf)
                    .collect(Collectors.toList()));
        }

        if (createdFrom != null) {
            params.withCreatedFrom(createdFrom);
        }

        if (createdTo != null) {
            params.withCreatedTo(createdTo);
        }

        if (modifiedFrom != null) {
            params.withModifiedFrom(modifiedFrom);
        }

        if (modifiedTo != null) {
            params.withModifiedTo(modifiedTo);
        }

        if (outputFormat != null) {
            params.withOutputFormat(RecordDumpServiceConnector.Params.OutputFormat.valueOf(outputFormat));
        }

        if (outputEncoding != null) {
            params.withOutputEncoding(outputEncoding);
        }

        System.out.println(params);

        if (cli.args.get("dryrun") != null) {
            System.out.println("Getting record count...");
            handleDryRun(params, url);
            System.out.println("Done!");
        } else {
            System.out.println("Exporting records...");
            handleDump(params, url, fileName);
            System.out.println("Done!");
        }
    }

    public static void handleDump(RecordDumpServiceConnector.Params params, String url, String fileName) {
        RecordDumpServiceConnector connector = RecordDumpServiceConnectorFactory.create(url);
        try {
            InputStream inputStream = connector.dumpAgencies(params);
            File file = new File(fileName);
            java.nio.file.Files.copy(
                    inputStream,
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            inputStream.close();
        } catch (RecordDumpServiceConnectorException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void handleDryRun(RecordDumpServiceConnector.Params params, String url) {
        RecordDumpServiceConnector connector = RecordDumpServiceConnectorFactory.create(url);
        try {
            InputStream inputStream = connector.dumpAgenciesDryRun(params);
            String result = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));

            System.out.println(result);
        } catch (RecordDumpServiceConnectorException e) {
            e.printStackTrace();
        }
    }

}
