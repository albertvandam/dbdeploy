package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import io.vandam.dbdeploy.databasestructure.ForeignKey;
import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.databasestructure.Table;

import java.util.ArrayList;
import java.util.Collection;

import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlForeignKey.getForeignKey;

class SqlForeignKeyDiff {
    static Collection<String> diffForeignKey(final Database source, final Database target) {
        final Collection<String> response = new ArrayList<>();

        // foreign key changes
        for (int i = 0; i < source.getTables().size(); i++) {
            final Table sourceTable = source.getTables().get(i);
            final Table targetTable = target.getTable(sourceTable.getSystemName());

            if (null == targetTable) {
                response.addAll(getForeignKey(sourceTable.getName(), sourceTable.getForeignKeys()));

            } else if (!sourceTable.getForeignKeys().isEmpty()) {
                if (targetTable.getForeignKeys().isEmpty()) {
                    response.addAll(getForeignKey(sourceTable.getName(), sourceTable.getForeignKeys()));
                } else if (!sourceTable.getForeignKeys().equals(targetTable.getForeignKeys())) {
                    for (final ForeignKey foreignKey : targetTable.getForeignKeys()) {
                        response.add("ALTER TABLE " + targetTable.getName() + '\n' +
                                "\tDROP FOREIGN KEY " + foreignKey.getName());
                    }
                    for (final ForeignKey foreignKey : sourceTable.getForeignKeys()) {
                        response.add("ALTER TABLE " + targetTable.getName() + '\n' +
                                '\t' + getForeignKey(foreignKey));
                    }
                }
            }
        }
        for (int i = 0; i < target.getTables().size(); i++) {
            final Table targetTable = target.getTables().get(i);

            final Table sourceTable = source.getTable(targetTable.getSystemName());

            if (null != sourceTable) {
                if (!targetTable.getForeignKeys().isEmpty()) {
                    if (sourceTable.getForeignKeys().isEmpty()) {
                        for (final ForeignKey foreignKey : targetTable.getForeignKeys()) {
                            response.add("ALTER TABLE " + targetTable.getName() + '\n' +
                                    "\tDROP FOREIGN KEY " + foreignKey.getName());
                        }
                    }
                }
            }
        }

        return response;
    }
}
