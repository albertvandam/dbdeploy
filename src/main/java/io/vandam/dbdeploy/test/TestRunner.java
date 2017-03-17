package io.vandam.dbdeploy.test;

import io.vandam.dbdeploy.sql.driver.DatabaseDriver;
import io.vandam.dbdeploy.sql.procedure.Direction;
import io.vandam.dbdeploy.sql.procedure.Parameter;
import io.vandam.dbdeploy.test.assertion.Assertion;
import io.vandam.dbdeploy.test.assertion.AssertionFieldSource;
import io.vandam.dbdeploy.test.assertion.UnknownComparatorException;
import io.vandam.dbdeploy.test.definition.TestDefinition;
import io.vandam.dbdeploy.test.definition.TestParameter;
import io.vandam.dbdeploy.test.definition.UnknownTestTypeException;
import io.vandam.dbdeploy.test.definition.TestStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {
    private final TestConfiguration m_testConfiguration;
    private final DatabaseDriver m_databaseDriver;

    public TestRunner(final DatabaseDriver databaseDriver, final TestConfiguration testConfiguration) {
        m_databaseDriver = databaseDriver;
        m_testConfiguration = testConfiguration;
    }

    public boolean runAll() throws SQLException {
        boolean response = true;

        for (final TestDefinition definition : m_testConfiguration.getDefinitions()) {
            response &= runTest(definition);
        }

        return response;
    }

    private void runBeforeTests(final Iterable<TestStatement> beforeTest) throws Exception {
        System.out.println("> Setup");

        for (final TestStatement testStatement : beforeTest) {
            System.out.println(">>> " + testStatement.getStatement());

            final List<Parameter> parameters = new ArrayList<>();
            final List<String> parameterValues = new ArrayList<>();
            for (final TestParameter testParameter : testStatement.getParameters()) {
                if (Direction.OUT == testParameter.getDirection()) {
                    throw new Exception("Before test cannot use OUT parameters");
                }

                parameters.add(new Parameter(testParameter.getDirection(), testParameter.getType()));
                parameterValues.add(testParameter.getValue());
            }

            switch (testStatement.getType()) {
                case SQL_STATEMENT:
                    m_databaseDriver.query(testStatement.getStatement(), parameterValues, false);
                    break;

                case STORED_PROCEDURE:
                    m_databaseDriver.call(testStatement.getStatement(), parameters, parameterValues, false);
                    break;

                default:
                    throw new UnknownTestTypeException("Unknown test type " + testStatement.getType());
            }
        }
    }

    private boolean runTest(final TestDefinition definition) throws SQLException {
        final boolean response;

        if (definition.isEnabled()) {
            System.out.println("Running test [" + definition.getId() + "] " + definition.getDescription());

            final Savepoint savepoint = m_databaseDriver.setSavePoint("SQL_Testing_" + definition.getId());

            int passed = 0;
            try {
                runBeforeTests(definition.getBeforeTest());

                final List<Parameter> parameters = new ArrayList<>();
                final List<String> parameterValues = new ArrayList<>();
                for (final TestParameter testParameter : definition.getTest().getParameters()) {
                    parameters.add(new Parameter(testParameter.getDirection(), testParameter.getType()));
                    parameterValues.add(testParameter.getValue());
                }

                final ResultSet resultSet;
                switch (definition.getTest().getType()) {
                    case SQL_STATEMENT:
                        System.out.println("> Execute query");
                        resultSet = m_databaseDriver.query(definition.getTest().getStatement(), parameterValues, true).getResultSet();
                        break;

                    case STORED_PROCEDURE:
                        System.out.println("> Call stored procedure");
                        resultSet = m_databaseDriver.call(definition.getTest().getStatement(), parameters, parameterValues, true).getResultSet();
                        break;

                    default:
                        throw new UnknownTestTypeException("Unknown test type " + definition.getTest().getType());
                }

                if (null == resultSet) {
                    System.err.println("> No resultset");
                } else {
                    System.out.println("> Perform assertions");
                    passed = assertResponse(definition.getAssertions(), parameterValues, resultSet);
                    resultSet.close();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }

            try {
                m_databaseDriver.rollback(savepoint);
            } catch (final Exception e) {
                e.printStackTrace();
            }

            System.out.println("Passed " + passed + " of " + definition.getAssertions().size() + " tests");

            response = passed == definition.getAssertions().size();
            System.out.println(response ? "PASSED" : "FAILED");
        } else {
            System.out.println("Skipping test [" + definition.getId() + "] " + definition.getDescription());
            response = true;
        }

        return response;
    }

    private static int assertResponse(final Iterable<Assertion> assertions, final List<String> parameterValues, final ResultSet resultSet) throws UnknownComparatorException, SQLException {
        int response = 0;

        int totalRecords = 0;

        if (null != resultSet) {
            for (final Assertion assertion : assertions) {
                totalRecords = 0;

                while (resultSet.next()) {
                    totalRecords++;

                    if ((AssertionFieldSource.RESULT_SET == assertion.getField().getSource()) && (totalRecords == assertion.getField().getRow())) {
                        if (assertion.compare(resultSet.getString(assertion.getField().getName()))) {
                            response++;
                        }
                    }
                }
            }
        }

        for (final Assertion assertion : assertions) {
            final String value;

            switch (assertion.getField().getSource()) {
                case TOTAL_RECORDS:
                    value = Integer.toString(totalRecords);
                    break;

                case OUT_PARAMETER:
                    final int parameterNumber = Integer.parseInt(assertion.getField().getName());
                    value = parameterValues.get(parameterNumber);
                    break;

                default:
                    value = null;
                    break;
            }

            if (null != value) {
                if (assertion.compare(value)) {
                    response++;
                }
            }
        }

        return response;
    }
}
