package io.vandam.dbdeploy.sql.static_data;

import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.sql.driver.DatabaseDriver;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PopulateTable {
    public static void populateTable(final DatabaseDriver databaseDriver, final Map<String, String> differences,
                                     final String target, final boolean applyChanges) throws Exception {
        for (final String tableName : differences.keySet()) {
            switch (differences.get(tableName)) {
                case "CLEAN":
                    clean(databaseDriver, tableName, applyChanges);
                    break;

                case "POPULATE":
                    populate(databaseDriver, tableName, applyChanges);
                    break;

                case "CHANGE":
                    change(databaseDriver, tableName, target, applyChanges);
                    break;
            }
        }
    }

    private static void clean(final DatabaseDriver databaseDriver, final String tableName, final boolean applyChanges)
            throws SQLException {
        final String cleanSql = "DELETE FROM " + tableName;
        System.out.println(cleanSql);
        if (applyChanges) {
            databaseDriver.query(cleanSql, null, false);
        }
    }

    private static void doInsert(final DatabaseDriver databaseDriver, final String tableName, final Record record,
                                 final List<AdditionalData> additionalData, final boolean applyChanges) throws SQLException {
        final List<String> parameters = new ArrayList<>();
        final StringBuilder sql = new StringBuilder();
        final StringBuilder displaySql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append("(\n");
        displaySql.append("INSERT INTO ").append(tableName).append("(\n");

        boolean firstColumn = true;
        for (final Column column : record.getColumns()) {
            if (firstColumn) {
                firstColumn = false;
            } else {
                sql.append(", ");
                displaySql.append(", ");
            }
            sql.append(column.getColumnName());
            displaySql.append(column.getColumnName());
        }
        for (final AdditionalData column : additionalData) {
            if (firstColumn) {
                firstColumn = false;
            } else {
                sql.append(", ");
                displaySql.append(", ");
            }
            sql.append(column.getField());
            displaySql.append(column.getField());
        }

        sql.append("\n) VALUES (\n");
        displaySql.append("\n) VALUES (\n");

        boolean firstValue = true;
        for (final Column column : record.getColumns()) {
            if (firstValue) {
                firstValue = false;
            } else {
                sql.append(", ");
                displaySql.append(", ");
            }
            sql.append('?');
            displaySql.append('\'').append(column.getValue()).append('\'');
            parameters.add(column.getValue());
        }
        for (final AdditionalData column : additionalData) {
            if (firstValue) {
                firstValue = false;
            } else {
                sql.append(", ");
                displaySql.append(", ");
            }

            displaySql.append(column.getValue());
            sql.append(column.getValue());
        }

        sql.append("\n)");
        displaySql.append("\n)");

        System.out.println(displaySql);
        if (applyChanges) {
            databaseDriver.query(sql.toString(), parameters, false);
        }
    }

    private static void doUpdate(final DatabaseDriver databaseDriver, final String tableName, final Record record,
                                 final List<AdditionalData> additionalData, final boolean applyChanges) throws Exception {
        final List<String> primaryKey = Database.getPrimaryKey(tableName);

        if ((null == primaryKey) || primaryKey.isEmpty()) {
            throw new Exception("No primary key defined for " + tableName);
        }

        final StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableName).append(" SET ");

        final StringBuilder displaySql = new StringBuilder();
        displaySql.append("UPDATE ").append(tableName).append(" SET ");

        boolean firstValue = true;

        final List<String> parameters = new ArrayList<>();
        for (final Column column : record.getColumns()) {
            if (firstValue) {
                firstValue = false;
            } else {
                sql.append(", ");
                displaySql.append(", ");
            }
            sql.append(column.getColumnName()).append("=?");

            String value = column.getValue();
            if ((null == value) || "null".equals(value)) {
                value = null;
                displaySql.append(column.getColumnName()).append("=null");
            } else {
                displaySql.append(column.getColumnName()).append("='").append(value).append('\'');
            }

            parameters.add(value);
        }

        for (final AdditionalData column : additionalData) {
            if (firstValue) {
                firstValue = false;
            } else {
                sql.append(", ");
                displaySql.append(", ");
            }

            sql.append(column.getField()).append('=').append(column.getValue());
            displaySql.append(column.getField()).append('=').append(column.getValue());
        }

        sql.append(" WHERE ");
        displaySql.append(" WHERE ");

        final WhereClauseBuilder whereClauseBuilder = new WhereClauseBuilder();
        whereClauseBuilder.build(primaryKey, record);
        sql.append(whereClauseBuilder.getSql());
        displaySql.append(whereClauseBuilder.getDisplaySql());
        parameters.addAll(whereClauseBuilder.getParameters());

        System.out.println(displaySql);
        if (applyChanges) {
            databaseDriver.query(sql.toString(), parameters, false);
        }
    }

    private static void doDelete(final DatabaseDriver databaseDriver, final String tableName, final Record record,
                                 final boolean applyChanges) throws Exception {
        final List<String> primaryKey = Database.getPrimaryKey(tableName);

        final StringBuilder sql = new StringBuilder();
        final StringBuilder displaySql = new StringBuilder();

        final List<String> parameters = new ArrayList<>();

        // sql.append("DELETE FROM ").append(tableName);
        // displaySql.append("DELETE FROM ").append(tableName);

        sql.append("UPDATE ").append(tableName).append(" SET STATUS='D'");
        displaySql.append("UPDATE ").append(tableName).append(" SET STATUS='D'");

        if ((null == primaryKey) || primaryKey.isEmpty()) {
            throw new Exception("No primary key defined for " + tableName);
        }

        sql.append(" WHERE ");
        displaySql.append(" WHERE ");

        final WhereClauseBuilder whereClauseBuilder = new WhereClauseBuilder();
        whereClauseBuilder.build(primaryKey, record);
        sql.append(whereClauseBuilder.getSql());
        displaySql.append(whereClauseBuilder.getDisplaySql());
        parameters.addAll(whereClauseBuilder.getParameters());

        System.out.println(displaySql);
        if (applyChanges) {
            databaseDriver.query(sql.toString(), parameters, false);
        }
    }

    private static void populate(final DatabaseDriver databaseDriver, final String tableName,
                                 final boolean applyChanges) throws Exception {
        final StaticData staticData = StaticData.fromXml("sql_source/static_data/" + tableName);
        for (final String key : staticData.getKeys()) {
            final Record record = staticData.getRecord(key);

            doInsert(databaseDriver, staticData.getTableName(), record, staticData.getAdditionalData(), applyChanges);
        }
    }

    private static void change(final DatabaseDriver databaseDriver, final String tableName, final String target,
                               final boolean applyChanges) throws Exception {
        final StaticData sourceData = StaticData.fromXml("sql_source/static_data/" + tableName);
        final StaticData targetData = StaticData.fromXml("sql_" + target + "/static_data/" + tableName);

        for (final String key : sourceData.getKeys()) {
            final Record sourceRecord = sourceData.getRecord(key);
            if (targetData.containsRecord(key)) {
                final Record targetRecord = targetData.getRecord(key);

                if (!sourceRecord.equals(targetRecord)) {
                    doUpdate(databaseDriver, sourceData.getTableName(), sourceRecord, sourceData.getAdditionalData(), applyChanges);
                }
            } else {
                doInsert(databaseDriver, sourceData.getTableName(), sourceRecord, sourceData.getAdditionalData(), applyChanges);
            }
        }
        for (final String key : targetData.getKeys()) {
            if (!sourceData.containsRecord(key)) {
                doDelete(databaseDriver, sourceData.getTableName(), targetData.getRecord(key), applyChanges);
            }
        }
    }
}
