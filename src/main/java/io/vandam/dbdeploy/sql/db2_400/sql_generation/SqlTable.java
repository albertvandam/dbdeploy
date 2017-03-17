package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import io.vandam.dbdeploy.sql.db2_400.InvalidIdentityColumnException;
import io.vandam.dbdeploy.databasestructure.Column;
import io.vandam.dbdeploy.databasestructure.Table;
import io.vandam.dbdeploy.sql.db2_400.InvalidDefaultValueException;

import java.util.ArrayList;
import java.util.List;

import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlColumn.getColumn;

class SqlTable {
    static List<String> getTable(final Table table) throws InvalidIdentityColumnException, InvalidDefaultValueException {
        final List<String> response = new ArrayList<>();

        final StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(table.getName()).append(' ');

        System.out.println("Sysname: " + table.getSystemName());
        System.out.println("Name: " + table.getName());
        if (!table.getSystemName().equals(table.getName())) {
            sql.append("FOR SYSTEM NAME ").append(table.getSystemName()).append(' ');
        }

        sql.append("(\n");

        for (int j = 0; j < table.getColumns().size(); j++) {
            final Column col = table.getColumns().get(j);

            if (0 != j) {
                sql.append(",\n");
            }

            sql.append('\t').append(col.getName()).append(" FOR ").append(col.getSystemName()).append(' ').append(SqlColumn.getColumn(col, true));
        }

        if (!table.getPrimaryKey().isEmpty()) {
            sql.append(",\n\t").append(SqlPrimaryKey.getPrimaryKey(table.getSystemName(), table.getPrimaryKey()));
        }

        sql.append("\n) RCDFMT ").append(table.getSystemName()).append('R');
        response.add(sql.toString());

        if (!table.getDescription().isEmpty()) {
            response.add("LABEL ON TABLE " + table.getName() + " IS '" + table.getDescription() + '\'');
        }

        final StringBuilder labels = new StringBuilder();
        for (int j = 0; j < table.getColumns().size(); j++) {
            Column column = table.getColumns().get(j);
            if (column == null) {
                System.out.println("Column #" + j + " is null in " + table.getName());
            } else {
                String description = column.getDescription();
                if (description != null && !description.isEmpty()) {
                    if (0 != labels.length()) {
                        labels.append(",\n");
                    }

                    labels.append('\t').append(table.getColumns().get(j).getName()).append(" TEXT IS '").append(table.getColumns().get(j).getDescription()).append('\'');
                }
            }
        }
        if (0 != labels.length()) {
            response.add("LABEL ON COLUMN " + table.getName() + "(\n" + labels + "\n)");
        }

        return response;
    }
}
