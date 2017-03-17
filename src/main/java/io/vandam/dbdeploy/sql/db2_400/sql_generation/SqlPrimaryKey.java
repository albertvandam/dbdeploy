package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import java.util.List;

class SqlPrimaryKey {
    static String getPrimaryKey(final String tableSystemName, final List<String> columns) {
        final StringBuilder sql = new StringBuilder();

        sql.append("CONSTRAINT PK").append(tableSystemName).append(" PRIMARY KEY (");

        for (int j = 0; j < columns.size(); j++) {
            if (0 != j) {
                sql.append(',');
            }

            sql.append(columns.get(j));
        }

        sql.append(')');

        return sql.toString();
    }
}
