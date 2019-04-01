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

        List<Integer> agencies = cli.args.getList("agencies");
        String recordStatus = cli.args.get("status");
        List<String> recordTypes = cli.args.get("type");

        String outputFormat = cli.args.get("format");
        String outputEncoding = cli.args.get("encoding");

        String createdFrom = cli.args.get("created_from");
        String createdTo = cli.args.get("created_to");
        String modifiedFrom = cli.args.get("modified_from");
        String modifiedTo = cli.args.get("modified_to");

        File file = cli.args.get("file");
        String url = cli.args.get("url");

        boolean dryrun = cli.args.get("dryrun") != null ? cli.args.get("dryrun") : false;

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
            if ("LATIN-1".equalsIgnoreCase(outputEncoding)) {
                // Small hack as 'LATIN-1' is not accepted by java
                params.withOutputEncoding("LATIN1");
            } else {
                params.withOutputEncoding(outputEncoding);
            }
        }

        try {
            dump(params, url, file, dryrun);
        } catch (RuntimeException ex) {
            System.out.println("Caught exception - operation aborted");
        }
    }

    public static void dump(RecordDumpServiceConnector.Params params, String url, File file, boolean dryrun) {
        RecordDumpServiceConnector connector = RecordDumpServiceConnectorFactory.create(url);

        try {
            System.out.println("Getting record count...");
            InputStream inputStream = connector.dumpAgenciesDryRun(params);
            String result = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
            System.out.println(result);

            if (!dryrun) {
                System.out.println("Exporting records...");
                inputStream = connector.dumpAgencies(params);
                java.nio.file.Files.copy(
                        inputStream,
                        file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                inputStream.close();
            }

            System.out.println("Done");
        } catch (RecordDumpServiceConnectorUnexpectedStatusCodeValidationException ex) {
            System.out.println("Validation error!");
            for (ParamsValidationItem paramsValidationItem : ex.getParamsValidation().getErrors()) {
                System.out.println(String.format("Field %s: %s", paramsValidationItem.getFieldName(), paramsValidationItem.getMessage()));
                if ("recordType".equals(paramsValidationItem.getFieldName()) && paramsValidationItem.getMessage().contains("The field is required")) {
                    // Add extra help text if the field is recordType
                    // Indent so indentation matches previous message
                    System.out.println("                  Please add -t TYPE [TYPE ...], --type TYPE [TYPE ...]. See rrdump --help for more info");
                }
            }
            throw new RuntimeException("Validation failed!");
        } catch (RecordDumpServiceConnectorException | IOException ex) {
            System.out.println("Unexpected error!");
            System.out.println(ex.getMessage());
            throw new RuntimeException("Unexpected error");
        }
    }


}
