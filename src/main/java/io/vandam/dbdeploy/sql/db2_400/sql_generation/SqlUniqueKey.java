package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import io.vandam.dbdeploy.databasestructure.UniqueKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class SqlUniqueKey {
    static String getUniqueKey(final UniqueKey uniqueKey) {
        final StringBuilder sql = new StringBuilder();

        sql.append("ADD CONSTRAINT ").append(uniqueKey.getName()).append(" UNIQUE (\n");

        for (int k = 0; k < uniqueKey.getColumns().size(); k++) {
            if (0 != k) {
                sql.append(",\n");
            }
            sql.append("\t\t").append(uniqueKey.getColumns().get(k));
        }

        sql.append("\n\t)");

        return sql.toString();
    }

    static List<String> getUniqueKey(final String tableName, final Collection<UniqueKey> uniqueKeys) {
        final List<String> response = new ArrayList<>();

        if (!uniqueKeys.isEmpty()) {
            for (final UniqueKey uniqueKey : uniqueKeys) {
                response.add("ALTER TABLE " + tableName + "\n\t" + getUniqueKey(uniqueKey));
            }
        }

        return response;
    }
}
