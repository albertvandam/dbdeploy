package io.vandam.dbdeploy;

import io.vandam.dbdeploy.configuration.Configuration;

import javax.xml.bind.JAXBException;

import static io.vandam.dbdeploy.CompareAndApply.compareAndApply;
import static io.vandam.dbdeploy.DataFromTextFile.importDataFromTxtFile;
import static io.vandam.dbdeploy.Help.showDatabaseFormatHelp;
import static io.vandam.dbdeploy.Help.showHelp;
import static io.vandam.dbdeploy.Help.showTestFormatHelp;
import static io.vandam.dbdeploy.Import.importDatabase;
import static io.vandam.dbdeploy.Import.importStaticData;
import static io.vandam.dbdeploy.Test.test;
import static io.vandam.dbdeploy.configuration.Initialise.createSampleTestConfig;
import static io.vandam.dbdeploy.configuration.Initialise.initialise;
import static io.vandam.dbdeploy.utility.FileUtility.fileExists;

/**
 * Main class
 */
final class DBDeploy {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws JAXBException          the JAXB exception
     * @throws ClassNotFoundException the class not found exception
     */
    public static void main(final String[] args) throws Exception {
        String configFile = "conf/config.xml";

        Activity activity = Activity.HELP;
        String target = null;
        String tableName = null;
        String fileName = null;

        int i = 0;
        while (i < args.length) {
            switch (args[i].toLowerCase()) {
                case "init":
                    activity = Activity.INITIALISE;
                    break;

                case "help":
                    if (args.length > (i+1)) {
                        switch (args[i+1]) {
                            case "testformat":
                                showTestFormatHelp();
                                break;

                            case "databaseformat":
                                showDatabaseFormatHelp();
                                break;

                            default:
                                showHelp();
                                break;
                        }
                    } else {
                        showHelp();
                    }
                    return;

                case "--config":
                    if (args.length > (i + 1)) {
                        configFile = args[i + 1];
                        i++;
                    } else {
                        System.err.println("Missing config file parameter");
                        return;
                    }
                    break;

                case "import":
                    activity = Activity.IMPORT;
                    break;

                case "importdata":
                    activity = Activity.IMPORT_DATA;

                    if (args.length > (i + 1)) {
                        tableName = args[i + 1];
                        i += 1;
                    } else {
                        System.err.println("Missing table name");
                        return;
                    }
                    break;

                case "compare":
                case "apply":
                    activity = Activity.valueOf(args[i].toUpperCase());

                    if (args.length > (i + 1)) {
                        target = args[i + 1];
                        i += 1;
                    } else {
                        System.err.println("Missing source parameter");
                        return;
                    }
                    break;

                case "importtxt":
                    activity = Activity.DATA_FROM_TXT;

                    if (args.length > (i + 2)) {
                        tableName = args[i + 1];
                        fileName = args[i + 2];
                        i += 2;
                    } else {
                        System.err.println("Missing table name and/or file name");
                        return;
                    }
                    break;

                case "test":
                    activity = Activity.TEST;

                    if (args.length > (i + 1)) {
                        target = args[i + 1];
                        i++;
                    } else {
                        System.err.println("Missing target parameter");
                        return;
                    }
                    break;

                case "testsample":
                    activity = Activity.CREATE_TEST_SAMPLE;
                    break;

                default:
                    System.err.println("Unknown parameter: " + args[i]);
                    break;
            }

            i++;
        }

        final Configuration config = loadConfig(configFile);
        switch (activity) {
            case INITIALISE:
                initialise(config, configFile);
                break;

            case IMPORT:
                importDatabase(config, "SOURCE");
                break;

            case COMPARE:
                compareAndApply(config, target, false);
                break;

            case APPLY:
                compareAndApply(config, target, true);
                break;

            case TEST:
                test(config, target);
                break;

            case IMPORT_DATA:
                importStaticData(config, "SOURCE", tableName);
                break;

            case DATA_FROM_TXT:
                importDataFromTxtFile(tableName, fileName);
                break;

            case CREATE_TEST_SAMPLE:
                createSampleTestConfig();
                break;

            default:
                showHelp();
                break;
        }
    }

    /**
     * Load config.
     *
     * @return true, if successful
     * @throws JAXBException the JAXB exception
     */
    private static Configuration loadConfig(final String configFile) throws JAXBException {
        if (fileExists(configFile)) {
            return Configuration.fromXml(configFile);
        }

        System.err.println("Cannot find configuration file " + configFile);
        return new Configuration();
    }
}
