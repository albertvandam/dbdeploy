package io.vandam.dbdeploy;

import io.vandam.dbdeploy.configuration.Configuration;
import io.vandam.dbdeploy.sql.driver.DatabaseDriver;
import io.vandam.dbdeploy.test.TestConfiguration;
import io.vandam.dbdeploy.utility.DirectoryListing;
import io.vandam.dbdeploy.test.TestRunner;

import javax.xml.bind.JAXBException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

class Test {

    /**
     * Run tests against target
     *
     * @param target Test target
     */
    static void test(final Configuration configuration, final String target) throws ClassNotFoundException, SQLException, JAXBException {
        final DatabaseDriver databaseDriver = new DatabaseDriver(configuration.getConfig(target), Connection.TRANSACTION_READ_COMMITTED);
        if (!databaseDriver.connect()) {
            System.err.println("Connection failed");
            return;
        }

        System.out.println(runTests(databaseDriver) ? "Tests passed" : "Tests failed");

        databaseDriver.close();
    }

    static boolean runTests(final DatabaseDriver databaseDriver) throws JAXBException, SQLException {
        boolean response = true;

        final List<String> testConfigurations = DirectoryListing.getFiles("tests", true);

        for (final String configFile : testConfigurations) {
            final TestConfiguration testConfiguration = TestConfiguration.fromXml(configFile);

            if (testConfiguration.isEnabled()) {
                System.out.println("Running test suite " + testConfiguration.getName());
                final TestRunner testRunner = new TestRunner(databaseDriver, testConfiguration);
                response &= testRunner.runAll();
            } else {
                System.out.println("Skipping test suite " + testConfiguration.getName());
            }
        }

        return response;
    }
}
