package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import io.vandam.dbdeploy.databasestructure.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class SqlIndex {
    static List<String> getIndex(final String tableName, final Index index) {
        final List<String> response = new ArrayList<>();

        final StringBuilder sql = new StringBuilder();
        sql.append("CREATE INDEX ").append(index.getName()).append('\n');
        if (!index.getName().equals(index.getSystemName())) {
            sql.append("\tFOR SYSTEM NAME ").append(index.getSystemName()).append('\n');
        }
        sql.append("\tON ").append(tableName).append(" (\n");
        for (int k = 0; k < index.getColumns().size(); k++) {
            if (0 != k) {
                sql.append(",\n");
            }
            sql.append("\t\t").append(index.getColumns().get(k)).append(" ASC");
        }
        sql.append("\n\t)");
        response.add(sql.toString());

        if ((null != index.getDescription()) && (0 != index.getDescription().length())) {
            response.add("LABEL ON INDEX " + index.getName() + " IS '" + index.getDescription() + '\'');
        }

        return response;
    }

    static Collection<String> getIndex(final String tableName, final Collection<Index> indexes) {
        final List<String> response = new ArrayList<>();

        if (!indexes.isEmpty()) {
            for (final Index index : indexes) {
                response.addAll(getIndex(tableName, index));
            }
        }

        return response;
    }
}
