package io.vandam.dbdeploy;

import io.vandam.dbdeploy.configuration.Configuration;
import io.vandam.dbdeploy.sql.IProcess;
import io.vandam.dbdeploy.sql.ProcessFactory;
import io.vandam.dbdeploy.sql.driver.DatabaseDriver;
import io.vandam.dbdeploy.utility.DirectoryListing;
import io.vandam.dbdeploy.configuration.DatabaseConfig;
import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.sql.driver.ScriptRunner;
import io.vandam.dbdeploy.sql.static_data.Record;
import io.vandam.dbdeploy.sql.static_data.StaticData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static io.vandam.dbdeploy.sql.static_data.PopulateTable.populateTable;
import static io.vandam.dbdeploy.utility.FileUtility.*;

class CompareAndApply {
    /**
     * Compare databases and apply changes.
     *
     * @param configuration configuration
     * @param target        the target
     * @param applyChanges  whether to apply changes
     * @throws Exception An error occurred
     */
    static void compareAndApply(final Configuration configuration, final String target, final boolean applyChanges) throws Exception {
        Import.importDatabase(configuration, target);

        if (!fileExists("conf/SOURCE.xml")) {
            System.err.println("Cannot find source structure");
            return;
        }

        if (!fileExists("conf/" + target + ".xml")) {
            System.err.println("Cannot find " + target + " structure");
            return;
        }

        final Database sourceDatabase = Database.fromXml("conf/SOURCE.xml");
        final Database targetDatabase = Database.fromXml("conf/" + target + ".xml");

        final DatabaseConfig targetConfig = configuration.getConfig(target);

        final IProcess dbProcess = ProcessFactory.getProcess(targetConfig.getDriverType());
        if (null == dbProcess) {
            return;
        }

        System.out.println("Determining structural differences");
        final List<String> diffList;
        try {
            diffList = dbProcess.getChangeSql(sourceDatabase, targetDatabase);
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        if (diffList.isEmpty()) {
            System.out.println("No structural changes to apply");
        }

        DatabaseDriver databaseDriver = null;
        if (applyChanges) {
            databaseDriver = new DatabaseDriver(targetConfig, Connection.TRANSACTION_READ_COMMITTED);
            if (!databaseDriver.connect()) {
                System.err.println("Connection failed");
                return;
            }
        }

        boolean success = true;
        if (applyChanges) {
            success = applyList(databaseDriver, diffList, "Applying structural changes script");
        } else {
            printList(diffList);
        }

        if (success) {
            try {
                final HashMap<String, String> differences = findRoutineDifferences(target, "trigger");
                final HashMap<String, String> sourceSql = getSources("sql_source/triggers");

                final Collection<String> changeList = getRoutineChangeSql(differences, sourceSql, "trigger");
                
                if (changeList.isEmpty()) {
                	System.out.println("No trigger changes to apply");
                }

                if (applyChanges) {
                    success = applyList(databaseDriver, changeList, "Creating triggers");
                } else {
                    printList(changeList);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                success = false;
            }
        }

        if (success) {
            try {
                final HashMap<String, String> differences = findRoutineDifferences(target, "proc");
                final HashMap<String, String> sourceSql = getSources("sql_source/stored_procedures");

                final Collection<String> changeList = getRoutineChangeSql(differences, sourceSql, "proc");
                
                if (changeList.isEmpty()) {
                	System.out.println("No procedure changes to apply");
                }

                if (applyChanges) {
                    success = applyList(databaseDriver, changeList, "Creating procedures");
                } else {
                    printList(changeList);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                success = false;
            }
        }

        if (success) {
            try {
                final HashMap<String, String> differences = findDataDifferences(target);
                
                if (differences.isEmpty()) {
                	System.out.println("No data changes to apply");
                }

                populateTable(databaseDriver, differences, target, applyChanges);
            } catch (final Exception e) {
                e.printStackTrace();
                success = false;
            }
        }

        if (success && applyChanges) {
            success = Test.runTests(databaseDriver);
            System.out.println(success ? "Tests passed" : "Tests failed");
        }

        if (applyChanges) {
            try {
                if (success) {
                    System.out.println("Comitting");
                    databaseDriver.commit();
                } else {
                    System.err.println("Rolling back");
                    databaseDriver.rollback();
                }
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }

            databaseDriver.close();
        }

        // clean up
        if (!deleteFile("conf/" + target + ".xml")) {
            System.out.println("Removal of " + target + ".xml failed");
        }
        if (!deleteDirectory("sql_" + target)) {
            System.out.println("Removal of sql_" + target + " failed");
        }
    }

    private static boolean applyList(final DatabaseDriver databaseDriver, final Collection<String> sqlQueries, final String type) {
        boolean success = true;

        if (!sqlQueries.isEmpty()) {
            try {
                System.out.println(type);
                ScriptRunner.runScript(databaseDriver, sqlQueries, true);
            } catch (final Exception e) {
                System.err.println("Script executing failed");

                e.printStackTrace();

                success = false;
            }
        }

        return success;
    }

    private static void printList(final Iterable<String> sqlQueries) {
        for (final String sql : sqlQueries) {
            System.out.println(sql + ';');
        }
    }

    private static HashMap<String, String> getSources(final String directoryName) throws IOException {
        final HashMap<String, String> sqlSources = new HashMap<>();

        final List<String> fileList = DirectoryListing.getFiles(directoryName, false);
        for (final String sourceFile : fileList) {
            if (!fileExists(directoryName + '/' + sourceFile)) {
                throw new FileNotFoundException("Source file " + directoryName + '/' + sourceFile + " does not exist");
            }

            sqlSources.put(sourceFile, readFileContent(directoryName + '/' + sourceFile));
        }

        return sqlSources;
    }

    private static Collection<String> getRoutineChangeSql(final Map<String, String> differences, final Map<String, String> sourceSql, final String type) throws Exception {
        final Collection<String> changeList = new ArrayList<>();

        String routineType = "trigger".equals(type) ? "TRIGGER" : "";
        routineType = "proc".equals(type) ? "PROCEDURE" : routineType;

        if (routineType.isEmpty()) {
            throw new Exception("Unknown routine type " + type);
        }

        for (final String routineName : differences.keySet()) {
            switch (differences.get(routineName)) {
                case "DROP":
                    changeList.add("DROP " + routineType + ' ' + routineName.replace(".sql", ""));
                    break;

                case "CREATE":
                    if (!sourceSql.containsKey(routineName)) {
                        System.out.println(sourceSql);
                        throw new Exception("Must create " + routineName + " but don't have source");
                    }
                    changeList.add(sourceSql.get(routineName));
                    break;

                default:
                    throw new Exception("Unknown change action for " + routineName + ' ' + differences.get(routineName));
            }
        }

        return changeList;
    }

    private static HashMap<String, String> findRoutineDifferences(final String target, final String type) throws Exception {
        final HashMap<String, String> response = new HashMap<>();

        String sourceDirectory = "trigger".equals(type) ? "triggers" : "";
        sourceDirectory = "proc".equals(type) ? "stored_procedures" : sourceDirectory;

        if (sourceDirectory.isEmpty()) {
            throw new Exception("Unknown routine type: " + type);
        }

        final String realSourceDirectory = "sql_source/" + sourceDirectory;
        final String realTargetDirectory = "sql_" + target + '/' + sourceDirectory;

        final List<String> sourceRoutineList = DirectoryListing.getFiles(realSourceDirectory, false);
        final List<String> targetRoutineList = DirectoryListing.getFiles(realTargetDirectory, false);
        for (final String routineName : sourceRoutineList) {
            if (targetRoutineList.contains(routineName)) {
                final String sourceSql = readFileContent(realSourceDirectory + '/' + routineName).replace("\t", "").replace("\n", "").replace("\r", "").replace(" ", "");
                final String targetSql = readFileContent(realTargetDirectory + '/' + routineName).replace("\t", "").replace("\n", "").replace("\r", "").replace(" ", "");

                if (!sourceSql.equals(targetSql)) {
                    response.put(routineName, "CREATE");
                    System.out.println("Replace " + type + ' ' + routineName);
                }
            } else {
                response.put(routineName, "CREATE");
                System.out.println("Create " + type + ' ' + routineName);
            }
        }
        for (final String routineName : targetRoutineList) {
            if (!sourceRoutineList.contains(routineName)) {
                response.put(routineName, "DROP");
                System.out.println("Drop " + type + ' ' + routineName);
            }
        }

        return response;
    }

    private static HashMap<String, String> findDataDifferences(final String target) throws Exception {
        final HashMap<String, String> response = new HashMap<>();

        final String realSourceDirectory = "sql_source/static_data";
        final String realTargetDirectory = "sql_" + target + "/static_data";
        
                final List<String> sourceTableList = DirectoryListing.getFiles(realSourceDirectory, false);
        final List<String> targetTableList = DirectoryListing.getFiles(realTargetDirectory, false);
        
        for (final String tableName : sourceTableList) {
            if (targetTableList.contains(tableName)) {
                final StaticData sourceData = StaticData.fromXml(realSourceDirectory + '/' + tableName);
                final StaticData targetData = StaticData.fromXml(realTargetDirectory + '/' + tableName);

                if (!sourceData.getRecords().equals(targetData.getRecords())) {
                    response.put(tableName, "CHANGE");
                    for (final String key : sourceData.getKeys()) {
                        if (targetData.containsRecord(key)) {
                            final Record sourceRecord = sourceData.getRecord(key);
                            final Record targetRecord = targetData.getRecord(key);

                            if (!sourceRecord.equals(targetRecord)) {
                                System.out.println("> Change " + key);
                            }
                        } else {
                            System.out.println("> Add " + key);
                        }
                    }
                    for (final String key : targetData.getKeys()) {
                        if (!sourceData.containsRecord(key)) {
                            System.out.println("> Remove " + key);
                        }
                    }
                }
            } else {
                response.put(tableName, "POPULATE");
                System.out.println("Populate table " + tableName);
            }
        }
        for (final String tableName : targetTableList) {
            if (!sourceTableList.contains(tableName)) {
                response.put(tableName, "CLEAN");
                System.out.println("Clean table " + tableName);
            }
        }

        return response;
    }
}
