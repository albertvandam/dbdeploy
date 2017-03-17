package io.vandam.dbdeploy.configuration;

import io.vandam.dbdeploy.sql.driver.DriverType;
import io.vandam.dbdeploy.sql.procedure.ParameterType;
import io.vandam.dbdeploy.test.TestConfiguration;
import io.vandam.dbdeploy.test.assertion.Assertion;
import io.vandam.dbdeploy.test.assertion.AssertionComparator;
import io.vandam.dbdeploy.test.assertion.AssertionField;
import io.vandam.dbdeploy.test.definition.TestDefinition;
import io.vandam.dbdeploy.test.definition.TestParameter;
import io.vandam.dbdeploy.test.definition.TestType;
import io.vandam.dbdeploy.utility.FileUtility;
import io.vandam.dbdeploy.xml.XsdGenerator;
import io.vandam.dbdeploy.sql.procedure.Direction;
import io.vandam.dbdeploy.test.assertion.AssertionFieldSource;
import io.vandam.dbdeploy.test.definition.TestStatement;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public class Initialise {
    private static boolean getConfig(final Configuration configuration, final boolean isSource) {
        final String configName;
        if (isSource) {
            configName = "SOURCE";
        } else {
            System.out.print("Datasource identifier: ");
            configName = System.console().readLine();
        }

        if (configName.trim().isEmpty()) {
            return true;
        }

        if (null != configuration.getConfig(configName)) {
            System.out.println("Datasource configuration " + configName + " already exists");
            return getConfig(configuration, isSource);
        }

        final DatabaseConfig config = new DatabaseConfig();
        config.setName(configName);

        System.out.print("Host name/IP: ");
        final String hostName = System.console().readLine();
        config.setHostname(hostName);

        System.out.print("Database name: ");
        final String databaseName = System.console().readLine();
        config.setDatabaseName(databaseName);

        System.out.print("Schema name: ");
        final String schemaName = System.console().readLine();
        config.setSchemaName(schemaName);

        System.out.print("User name: ");
        final String userName = System.console().readLine();
        config.setUsername(userName);

        System.out.print("Password: ");
        final String password = System.console().readLine();
        config.setPassword(password);

        if (isSource) {
            System.out.println("Driver: ");
            System.out.println("1 = DB2 on IBM i");
            System.out.println("2 = MySQL");
            final String driverType = System.console().readLine();
            if ("1".equals(driverType)) {
                config.setDriverType(DriverType.DB2_400);
            } else if ("2".equals(driverType)) {
                config.setDriverType(DriverType.MYSQL);
            }
        } else {
            config.setDriverType(DriverType.DB2_400);
        }

        configuration.setConfig(config);

        return false;
    }

    /**
     * Initialise.
     *
     * @throws JAXBException the JAXB exception
     */

    public static void initialise(final Configuration configuration, final String configFile) throws JAXBException, IOException {
        if (!FileUtility.createDirectory("conf")) {
            System.err.println("Cannot create config directory");
            return;
        }
        if (!FileUtility.createDirectory("tests")) {
            System.err.println("Cannot create tests directory");
            return;
        }
        if (!FileUtility.createDirectory("sql_source")) {
            System.err.println("Cannot create SQL source directory");
            return;
        }
        if (!FileUtility.createDirectory("sql_source/triggers")) {
            System.err.println("Cannot create triggers directory");
            return;
        }
        if (!FileUtility.createDirectory("sql_source/stored_procedures")) {
            System.err.println("Cannot create stored procedures directory");
            return;
        }
        if (!FileUtility.createDirectory("sql_source/static_data")) {
            System.err.println("Cannot create static data directory");
            return;
        }

        System.out.println("Initialise configuration");

        boolean setSourceConfig = true;
        if (null != configuration.getConfig("SOURCE")) {
            final DatabaseConfig databaseConfig = configuration.getConfig("SOURCE");

            System.out.println("\n\nReference datasource already configured:");
            System.out.println("User: " + databaseConfig.getUsername());
            System.out.println("Host: " + databaseConfig.getHostname());
            System.out.println("Database: " + databaseConfig.getDatabaseName());
            System.out.println("Schema: " + databaseConfig.getSchemaName());
            System.out.println("\n");
            System.out.print("Keep this? [Y/N] ");

            final String keep = System.console().readLine().toUpperCase();
            if ("Y".equals(keep)) {
                setSourceConfig = false;
            } else {
                configuration.removeConfig("SOURCE");
            }
        }

        if (setSourceConfig) {
            System.out.println("\n\nPlease provide detail of reference datasource.");
            getConfig(configuration, true);
        }

        boolean done;
        do {
            System.out.println("\n\nPlease provide detail of new datasource. (EMPTY = quits)");
            done = getConfig(configuration, false);

        } while (!done);

        System.out.println("\nWriting configuration to " + configFile);
        configuration.toXml(configFile);

        createSampleTestConfig();
    }

    public static void createSampleTestConfig() throws JAXBException, IOException {
        final TestConfiguration testConfiguration = new TestConfiguration();
        testConfiguration.setName("Test suite #1");
        testConfiguration.setEnabled(false);

        final TestDefinition testDefinition = new TestDefinition();
        testDefinition.setId("TEST #1");
        testDefinition.setDescription("Test #1");
        testDefinition.setEnabled(true);

        final TestStatement beforeTestStatement = new TestStatement();
        beforeTestStatement.setType(TestType.SQL_STATEMENT);
        beforeTestStatement.setStatement("INSERT INTO SOME_TABLE (COL1, COL2, COL3) VALUES (?, ?, ?)");

        final TestParameter testParameter1 = new TestParameter();
        testParameter1.setValue("1");
        beforeTestStatement.getParameters().add(testParameter1);

        final TestParameter testParameter2 = new TestParameter();
        testParameter2.setValue("2");
        beforeTestStatement.getParameters().add(testParameter2);

        final TestParameter testParameter3 = new TestParameter();
        testParameter3.setValue("3");
        beforeTestStatement.getParameters().add(testParameter3);

        testDefinition.getBeforeTest().add(beforeTestStatement);

        final TestStatement testStatement = new TestStatement();
        testStatement.setType(TestType.STORED_PROCEDURE);
        testStatement.setStatement("CALL SOME_PROC(?, ?, ?)");

        final TestParameter testProcParameter1 = new TestParameter();
        testProcParameter1.setDirection(Direction.IN);
        testProcParameter1.setType(ParameterType.INTEGER);
        testProcParameter1.setValue("1");
        testStatement.getParameters().add(testProcParameter1);

        final TestParameter testProcParameter2 = new TestParameter();
        testProcParameter2.setDirection(Direction.INOUT);
        testProcParameter2.setType(ParameterType.CHAR);
        testProcParameter2.setValue("2");
        testStatement.getParameters().add(testProcParameter2);

        final TestParameter testProcParameter3 = new TestParameter();
        testProcParameter3.setDirection(Direction.OUT);
        testProcParameter3.setType(ParameterType.CHAR);
        testStatement.getParameters().add(testProcParameter3);

        testDefinition.setTest(testStatement);

        final Assertion assertion1 = new Assertion();
        assertion1.setDescription("Assertion #1");

        final AssertionField assertionField1 = new AssertionField();
        assertionField1.setSource(AssertionFieldSource.OUT_PARAMETER);
        assertionField1.setName("1");
        assertion1.setField(assertionField1);
        assertion1.setComparison(AssertionComparator.NOT_EQUAL);
        assertion1.setExpectedValue("2");
        testDefinition.getAssertions().add(assertion1);

        final Assertion assertion2 = new Assertion();
        assertion2.setDescription("Assertion #2");
        final AssertionField assertionField2 = new AssertionField();
        assertionField2.setSource(AssertionFieldSource.OUT_PARAMETER);
        assertionField2.setName("2");
        assertion2.setField(assertionField2);
        assertion2.setComparison(AssertionComparator.EQUAL);
        assertion2.setExpectedValue("3");
        testDefinition.getAssertions().add(assertion2);

        final Assertion assertion3 = new Assertion();
        assertion3.setDescription("Assertion #2");
        final AssertionField assertionField3 = new AssertionField();
        assertionField3.setSource(AssertionFieldSource.RESULT_SET);
        assertionField3.setRow(1);
        assertionField3.setName("COL2");
        assertion3.setField(assertionField3);
        assertion3.setComparison(AssertionComparator.EQUAL);
        assertion3.setExpectedValue("3");
        testDefinition.getAssertions().add(assertion3);

        final Assertion assertion4 = new Assertion();
        assertion4.setDescription("Assertion #2");
        final AssertionField assertionField4 = new AssertionField();
        assertionField4.setSource(AssertionFieldSource.TOTAL_RECORDS);
        assertion4.setField(assertionField4);
        assertion4.setComparison(AssertionComparator.EQUAL);
        assertion4.setExpectedValue("1");
        testDefinition.getAssertions().add(assertion4);

        testConfiguration.getDefinitions().add(testDefinition);

        System.out.println("\nCreating sample test configuration in tests/sample.xml");
        testConfiguration.toXml("tests/sample.xml");

        System.out.println("\nCreating test configuration XSD in TestConfiguration.xsd");
        XsdGenerator.createXsd(TestConfiguration.class, "TestConfiguration.xsd");
    }
}
