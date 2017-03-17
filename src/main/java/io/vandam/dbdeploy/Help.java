package io.vandam.dbdeploy;

import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.test.TestConfiguration;
import io.vandam.dbdeploy.xml.XsdGenerator;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static io.vandam.dbdeploy.configuration.Initialise.createSampleTestConfig;

class Help {

    /**
     * Show help.
     */
    static void showHelp() {
        System.out.println("Actions:");
        System.out.println("help                            This help screen\n");
        System.out.println("help testformat                 Test format XSD\n");
        System.out.println("help databaseformat             Database format XSD\n");
        System.out.println("init                            Initialise configuration\n");
        System.out.println("import                          Import database from source\n");
        System.out.println("importdata <tablename>          Import static data from <tablename> in source\n");
        System.out.println("compare <target>                Show changes to be applied on <target> when");
        System.out.println("                                compared to source\n");
        System.out.println("apply <target>                  Apply changes on <target> when compared to");
        System.out.println("                                source\n");
        System.out.println("importtxt <table> <filename>    Import static data for <table> from <filename>");
        System.out.println("                                in tab seperated format\n");
        System.out.println("");
        System.out.println("Options:");
        System.out.println("--config <file>                 Use <file> for configuration\n");
    }

    static void showTestFormatHelp() throws JAXBException, IOException {
        XsdGenerator.createXsd(TestConfiguration.class, "TestConfiguration.xsd");
        createSampleTestConfig();

        System.out.println("Test format:\n");
        System.out.println("> XSD: TestConfiguration.xsd");
        System.out.println("> Sample: tests/sample.xml\n");
        System.out.println("Statement Types:         SQL_STATEMENT | STORED_PROCEDURE\n");
        System.out.println("Parameter Directions:    IN | OUT | INOUT\n");
        System.out.println("Parameter Types:         CHAR | DECIMAL | INTEGER | VARCHAR\n");
        System.out.println("Assertion Comparators:   EQUAL | NOT_EQUAL\n");
        System.out.println("Assertion Source:        OUT_PARAMETER | RESULT_SET | TOTAL_RECORDS\n");
    }

    static void showDatabaseFormatHelp() throws JAXBException, IOException {
        XsdGenerator.createXsd(Database.class, "Database.xsd");

        System.out.println("Database format:\n");
        System.out.println("> XSD: Database.xsd");
    }
}
