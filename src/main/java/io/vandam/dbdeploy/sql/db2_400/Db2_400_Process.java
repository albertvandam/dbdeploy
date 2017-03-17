package io.vandam.dbdeploy.sql.db2_400;

import io.vandam.dbdeploy.sql.IProcess;
import io.vandam.dbdeploy.configuration.DatabaseConfig;
import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.procedure.Parameter;
import io.vandam.dbdeploy.procedure.Procedure;
import io.vandam.dbdeploy.sql.ISqlGeneration;
import io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlGeneration;
import io.vandam.dbdeploy.sql.driver.DatabaseDriver;
import io.vandam.dbdeploy.sql.driver.DriverType;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Db2_400_Process implements IProcess {
    /**
     * Import db2400 database.
     *
     * @param config the config
     * @return the database
     * @throws ClassNotFoundException the class not found exception
     */
    @Override
    public Database importDatabase(final DatabaseConfig config) throws ClassNotFoundException {
        final ImportDatabase ids = new ImportDatabase(config);
        return ids.getDatabase();
    }


    @Override
    public List<String> getChangeSql(final Database sourceDatabase, final Database targetDatabase) throws InvalidIdentityColumnException, InvalidDefaultValueException {
        final ISqlGeneration diff = new SqlGeneration();
        return diff.getDiffDatabase(sourceDatabase, targetDatabase);
    }

//    @Override
//    public List<String> getCreateSql(final Database database) throws InvalidIdentityColumnException, InvalidDefaultValueException {
//        final ISqlGeneration diff = new SqlGeneration();
//        return diff.getDatabase(database);
//    }

    @Override
    public void importStoredProcedures(final DatabaseConfig config, final String targetDirectory) throws SQLException, IOException, ClassNotFoundException {
        final Map<String, Procedure> procedures = new HashMap<>();

        final List<String> parameters = new ArrayList<>();
        parameters.add(config.getSchemaName());

        final List<String> queryList = new ArrayList<>();
        queryList.add("SELECT \n" +
                "SPECIFIC_NAME,\n" +
                "PARAMETER_MODE,\n" +
                "PARAMETER_NAME,\n" +
                "DATA_TYPE,\n" +
                "CHARACTER_MAXIMUM_LENGTH,\n" +
                "PARAMETER_DEFAULT\n" +
                "FROM QSYS2.PARAMETERS\n" +
                "WHERE SPECIFIC_SCHEMA=? \n" +
                "ORDER BY SPECIFIC_NAME, ORDINAL_POSITION");

        queryList.add("SELECT \n" +
                "ROUTINE_NAME,\n" +
                "SPECIFIC_NAME,\n" +
                "ROUTINE_BODY,\n" +
                "IS_DETERMINISTIC,\n" +
                "SQL_DATA_ACCESS,\n" +
                "RESULT_SETS,\n" +
                "COMMIT_ON_RETURN,\n" +
                "ROUTINE_TEXT,\n" +
                "ROUTINE_DEFINITION\n" +
                "FROM QSYS2.SYSPROCS \n" +
                "WHERE SPECIFIC_SCHEMA=? \n" +
                "ORDER BY ROUTINE_NAME");

        final DatabaseDriver databaseDriver = new DatabaseDriver(config, Connection.TRANSACTION_NONE);
        databaseDriver.connect();

        for (int i = 0; i < queryList.size(); i++) {
            final ResultSet resultSet = databaseDriver.query(queryList.get(i), parameters, true).getResultSet();

            while (resultSet.next()) {
                final String procedureName = resultSet.getString("SPECIFIC_NAME");
                final Procedure procedure;

                switch (i) {
                    case 0:
                        System.out.println("Reading parameter definitions for " + procedureName);

                        procedure = procedures.containsKey(procedureName) ? procedures.get(procedureName) : new Procedure();

                        procedure.setSystemName(procedureName);

                        final Parameter parameter = new Parameter();
                        parameter.setMode(resultSet.getString("PARAMETER_MODE"));
                        parameter.setName(resultSet.getString("PARAMETER_NAME"));
                        parameter.setType(resultSet.getString("DATA_TYPE"));
                        parameter.setLength(resultSet.getString("CHARACTER_MAXIMUM_LENGTH"));
                        parameter.setDefault(resultSet.getString("PARAMETER_DEFAULT"));

                        procedure.getParameters().add(parameter);

                        procedures.put(procedureName, procedure);
                        break;

                    case 1:
                        System.out.println("Reading routine body for " + procedureName);

                        if (procedures.containsKey(procedureName)) {
                            procedure = procedures.get(procedureName);

                            procedure.setName(resultSet.getString("ROUTINE_NAME"));
                            procedure.setType(resultSet.getString("ROUTINE_BODY"));
                            procedure.setDeterministic(resultSet.getString("IS_DETERMINISTIC"));
//                            procedure.setSqlAccess(resultSet.getString("SQL_DATA_ACCESS"));
                            procedure.setResultSetQty(resultSet.getString("RESULT_SETS"));
//                            procedure.setCommitOnReturn(resultSet.getString("COMMIT_ON_RETURN"));
                            procedure.setDescription(resultSet.getString("ROUTINE_TEXT"));
                            procedure.setBody(resultSet.getString("ROUTINE_DEFINITION"));

                        } else {
                            System.err.println("Don't have parameter definition for " + procedureName);
                        }
                        break;
                }
            }
            resultSet.close();
        }

        databaseDriver.close();

        int iCurrent = 0;
        final int iTotal = procedures.size();

        for (final Procedure procedure : procedures.values()) {
            iCurrent++;
            System.out.println("Writing " + iCurrent + '/' + iTotal + ": " + procedure.getSystemName() + " to disk");
            final FileWriter fileWriter = new FileWriter(targetDirectory + procedure.getName() + ".sql");
            fileWriter.write(procedure.getSql());
            fileWriter.flush();
            fileWriter.close();
        }

        System.out.println("Done importing stored procedures");
    }

    @Override
    public void importTriggers(final DatabaseConfig config, final String targetDirectory) throws IOException, SQLException, ClassNotFoundException {
        final DatabaseDriver databaseDriver = new DatabaseDriver(config, Connection.TRANSACTION_NONE);

        final List<String> parameters = new ArrayList<>();
        parameters.add(config.getSchemaName());

        databaseDriver.connect();

        final String sql = "SELECT\n" +
                "TRIGGER_NAME,\n" +
                "TRIGGER_TEXT,\n" +
                "ACTION_STATEMENT,\n" +
                "EVENT_MANIPULATION,\n" +
                "EVENT_OBJECT_TABLE,\n" +
                "ACTION_TIMING,\n" +
                "ACTION_REFERENCE_OLD_ROW,\n" +
                "ACTION_REFERENCE_NEW_ROW,\n" +
                "ACTION_ORIENTATION\n" +
                "FROM QSYS2.SYSTRIGGERS \n" +
                "WHERE TRIGGER_SCHEMA=? \n" +
                "ORDER BY TRIGGER_NAME";
        final ResultSet resultSet = databaseDriver.query(sql, parameters, true).getResultSet();

        while (resultSet.next()) {
            final String triggerName = resultSet.getString("TRIGGER_NAME");
            final String triggerText = resultSet.getString("TRIGGER_TEXT");
            final StringBuilder triggerSql = new StringBuilder();

            triggerSql.append("CREATE OR REPLACE TRIGGER ").append(triggerName).append('\n');
            triggerSql.append('\t').append(resultSet.getString("ACTION_TIMING")).append(' ').append(resultSet.getString("EVENT_MANIPULATION")).append(" ON ").append(resultSet.getString("EVENT_OBJECT_TABLE")).append('\n');
            if ((null != resultSet.getString("ACTION_REFERENCE_OLD_ROW")) || (null != resultSet.getString("ACTION_REFERENCE_NEW_ROW"))) {
                triggerSql.append("\tREFERENCING");
                if (null != resultSet.getString("ACTION_REFERENCE_OLD_ROW")) {
                    triggerSql.append(" OLD AS ").append(resultSet.getString("ACTION_REFERENCE_OLD_ROW"));
                }
                if (null != resultSet.getString("ACTION_REFERENCE_NEW_ROW")) {
                    triggerSql.append(" NEW AS ").append(resultSet.getString("ACTION_REFERENCE_NEW_ROW"));
                }
            }
            triggerSql.append('\n');
            triggerSql.append("\tFOR EACH ").append(resultSet.getString("ACTION_ORIENTATION")).append('\n');
            triggerSql.append(resultSet.getString("ACTION_STATEMENT").replace(config.getSchemaName() + " . ", "")).append('\n');
            if (null != triggerText) {
                triggerSql.append("LABEL ON TRIGGER ").append(triggerName).append(" TEXT IS '").append(triggerText).append("'\n");
            }

            System.out.println("Writing " + triggerName + " to disk");
            final FileWriter fileWriter = new FileWriter(targetDirectory + triggerName + ".sql");
            fileWriter.write(triggerSql.toString());
            fileWriter.flush();
            fileWriter.close();
        }

        System.out.println("Done importing triggers");

        databaseDriver.close();
    }

    @Override
    public DriverType getType() {
        return DriverType.DB2_400;
    }
}
