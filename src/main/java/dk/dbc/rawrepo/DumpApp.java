/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.rawrepo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

        try {
            final File outputFile = cli.args.get("file");
            final String url = cli.args.get("url");

            if (cli.args.getList("agencies") != null) {
                final RecordDumpServiceConnector.AgencyParams params = constructAgencyParams(cli);
                final boolean dryrun = cli.args.get("dryrun") != null ? cli.args.get("dryrun") : false;

                dump(params, url, outputFile, dryrun);
            } else {
                // The argparser ensures one of the arguments will always be present, so we don't need to check further
                final RecordDumpServiceConnector.RecordParams params = constructRecordParams(cli);
                final File recordFile = cli.args.get("records");
                try (final FileInputStream fileInputStream = new FileInputStream(recordFile)) {
                    final String body = new BufferedReader(new InputStreamReader(fileInputStream)).lines().collect(Collectors.joining("\n"));
                    dump(params, url, outputFile, body);
                }
            }
        } catch (RuntimeException ex) {
            System.out.println("Caught exception - operation aborted");
        } catch (IOException e) {
            System.out.println("Error accessing the file '" + cli.args.getString("records") + "'. Does it exist?");
        }
    }

    private static RecordDumpServiceConnector.AgencyParams constructAgencyParams(Cli cli) {
        final List<Integer> agencies = cli.args.getList("agencies");
        final String recordStatus = cli.args.get("status");
        final List<String> recordTypes = cli.args.get("type");

        final String outputFormat = cli.args.get("format");
        final String outputEncoding = cli.args.get("encoding");
        final String mode = cli.args.get("mode");

        final String createdFrom = cli.args.get("created_from");
        final String createdTo = cli.args.get("created_to");
        final String modifiedFrom = cli.args.get("modified_from");
        final String modifiedTo = cli.args.get("modified_to");

        final RecordDumpServiceConnector.AgencyParams params = new RecordDumpServiceConnector.AgencyParams()
                .withAgencies(agencies);

        if (!recordStatus.isEmpty()) {
            params.withRecordStatus(RecordDumpServiceConnector.AgencyParams.RecordStatus.valueOf(recordStatus));
        }

        if (recordTypes != null && !recordTypes.isEmpty()) {
            params.withRecordType(recordTypes.stream()
                    .map(RecordDumpServiceConnector.AgencyParams.RecordType::valueOf)
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
            params.withOutputFormat(RecordDumpServiceConnector.AgencyParams.OutputFormat.valueOf(outputFormat));
        }

        if (mode != null) {
            params.withMode(RecordDumpServiceConnector.AgencyParams.Mode.valueOf(mode));
        }

        if (outputEncoding != null) {
            if ("LATIN-1".equalsIgnoreCase(outputEncoding)) {
                // Small hack as 'LATIN-1' is not accepted by java
                params.withOutputEncoding("LATIN1");
            } else {
                params.withOutputEncoding(outputEncoding);
            }
        }

        return params;
    }

    private static RecordDumpServiceConnector.RecordParams constructRecordParams(Cli cli) {
        final String outputFormat = cli.args.get("format");
        final String outputEncoding = cli.args.get("encoding");

        final RecordDumpServiceConnector.RecordParams params = new RecordDumpServiceConnector.RecordParams();

        if (outputFormat != null) {
            params.withOutputFormat(RecordDumpServiceConnector.RecordParams.OutputFormat.valueOf(outputFormat));
        }

        if (outputEncoding != null) {
            if ("LATIN-1".equalsIgnoreCase(outputEncoding)) {
                // Small hack as 'LATIN-1' is not accepted by java
                params.withOutputEncoding("LATIN1");
            } else {
                params.withOutputEncoding(outputEncoding);
            }
        }

        return params;
    }

    private static void dump(RecordDumpServiceConnector.AgencyParams params, String url, File file, boolean dryrun) {
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

    private static void dump(RecordDumpServiceConnector.RecordParams params, String url, File file, String body) {
        RecordDumpServiceConnector connector = RecordDumpServiceConnectorFactory.create(url);

        try {
            System.out.println("Exporting records...");
            InputStream inputStream = connector.dumpRecords(params, body);
            java.nio.file.Files.copy(
                    inputStream,
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            inputStream.close();

            System.out.println("Done");
        } catch (RecordDumpServiceConnectorException | IOException ex) {
            System.out.println("Unexpected error!");
            System.out.println(ex.getMessage());
            throw new RuntimeException("Unexpected error");
        }
    }

}
