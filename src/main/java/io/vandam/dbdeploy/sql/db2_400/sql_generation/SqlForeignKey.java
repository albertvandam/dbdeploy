package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import io.vandam.dbdeploy.databasestructure.ForeignKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class SqlForeignKey {
    static String getForeignKey(final ForeignKey foreignKey) {
        final StringBuilder sql = new StringBuilder();

        sql.append("ADD CONSTRAINT ").append(foreignKey.getName()).append(" FOREIGN KEY (\n");

        for (int k = 0; k < foreignKey.getColumns().size(); k++) {
            if (0 != k) {
                sql.append(",\n");
            }
            sql.append("\t\t").append(foreignKey.getColumns().get(k));
        }

        sql.append("\n\t) REFERENCES ").append(foreignKey.getReferenceTable()).append(" (\n");

        for (int k = 0; k < foreignKey.getReferenceColumns().size(); k++) {
            if (0 != k) {
                sql.append(",\n");
            }
            sql.append("\t\t").append(foreignKey.getReferenceColumns().get(k));
        }

        sql.append("\n\t)\n");
        sql.append("\tON DELETE NO ACTION\n");
        sql.append("\tON UPDATE NO ACTION");

        return sql.toString();
    }

    static List<String> getForeignKey(final String tableName, final Collection<ForeignKey> foreignKeys) {
        final List<String> response = new ArrayList<>();

        if (!foreignKeys.isEmpty()) {
            for (final ForeignKey fk : foreignKeys) {
                response.add("ALTER TABLE " + tableName + "\n\t" + getForeignKey(fk));
            }
        }

        return response;
    }
}
