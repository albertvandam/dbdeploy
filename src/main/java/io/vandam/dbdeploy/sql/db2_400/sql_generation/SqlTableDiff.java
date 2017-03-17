package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import io.vandam.dbdeploy.sql.db2_400.InvalidIdentityColumnException;
import io.vandam.dbdeploy.databasestructure.Column;
import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.databasestructure.Table;
import io.vandam.dbdeploy.sql.db2_400.InvalidDefaultValueException;

import java.util.ArrayList;
import java.util.Collection;

import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlColumn.getColumn;
import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlPrimaryKey.getPrimaryKey;
import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlTable.getTable;

class SqlTableDiff {
    static Collection<String> diffTable(final Database source, final Database target) throws InvalidIdentityColumnException, InvalidDefaultValueException {
        final Collection<String> response = new ArrayList<>();

        // table structure changes
        for (int i = 0; i < source.getTables().size(); i++) {
            final Table sourceTable = source.getTables().get(i);
            final Table targetTable = target.getTable(sourceTable.getSystemName());

            if (null == targetTable) {
                response.addAll(getTable(sourceTable));

            } else {
                if (!sourceTable.getColumns().equals(targetTable.getColumns())) {
                    for (int j = 0; j < sourceTable.getColumns().size(); j++) {
                        final Column sourceColumn = sourceTable.getColumns().get(j);
                        final Column targetColumn = targetTable.getColumn(sourceColumn.getSystemName());

                        if (null == targetColumn) {
                            response.add("ALTER TABLE " + sourceTable.getName() + '\n' +
                                    "\tADD COLUMN " + sourceColumn.getName() + " FOR " +
                                    sourceColumn.getSystemName() + ' ' + SqlColumn.getColumn(sourceColumn, true));

                            response.add("LABEL ON COLUMN " + sourceTable.getName() + "(\n" +
                                    '\t' + sourceColumn.getName() + " TEXT IS '" + sourceColumn.getDescription() + "'\n" +
                                    ')');

                        } else if (!sourceColumn.equals(targetColumn)) {
                            response.add("ALTER TABLE " + sourceTable.getName() + '\n' +
                                    "\tALTER COLUMN " + sourceColumn.getName() + '\n' +
                                    "\tSET DATA TYPE " + SqlColumn.getColumn(sourceColumn, false));

                            if (!sourceColumn.getDescription().equals(targetColumn.getDescription())) {
                                response.add("LABEL ON COLUMN " + sourceTable.getName() + " (\n" +
                                        '\t' + sourceColumn.getName() + " TEXT IS '" + sourceColumn.getDescription() + "'\n" +
                                        ')');
                            }
                        }
                    }

                }

                boolean addDropPrimaryKey = false;
                boolean addAddPrimaryKey = false;
                if (!(sourceTable.getPrimaryKey().isEmpty() && targetTable.getPrimaryKey().isEmpty())) {
                    if (targetTable.getPrimaryKey().isEmpty()) {
                        addAddPrimaryKey = true;
                    } else if (sourceTable.getPrimaryKey().isEmpty()) {
                        addDropPrimaryKey = true;
                    } else if (!sourceTable.getPrimaryKey().equals(targetTable.getPrimaryKey())) {
                        addDropPrimaryKey = true;
                        addAddPrimaryKey = true;
                    }
                }

                if (addDropPrimaryKey) {
                    response.add("ALTER TABLE " + sourceTable.getName() + '\n' +
                            "\tDROP PRIMARY KEY");
                }
                if (addAddPrimaryKey) {
                    response.add("ALTER TABLE " + sourceTable.getName() + '\n' +
                            "\tADD " + getPrimaryKey(sourceTable.getSystemName(), sourceTable.getPrimaryKey()));
                }
            }
        }
        for (int i = 0; i < target.getTables().size(); i++) {
            final Table targetTable = target.getTables().get(i);
            final Table sourceTable = source.getTable(targetTable.getSystemName());

            if (null == sourceTable) {
                response.add("DROP TABLE " + targetTable.getName());

            } else {
                if (!targetTable.getColumns().equals(sourceTable.getColumns())) {
                    for (int j = 0; j < targetTable.getColumns().size(); j++) {
                        final Column targetColumn = targetTable.getColumns().get(j);
                        final Column sourceColumn = sourceTable.getColumn(targetColumn.getSystemName());

                        if (null == sourceColumn) {
                            response.add("ALTER TABLE " + targetTable.getName() + '\n' +
                                    "\tDROP COLUMN " + targetColumn.getName());
                        }
                    }
                }
            }
        }

        return response;
    }
}
