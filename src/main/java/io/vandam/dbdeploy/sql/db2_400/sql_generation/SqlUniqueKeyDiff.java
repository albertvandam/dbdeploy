package io.vandam.dbdeploy.sql.db2_400.sql_generation;


import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.databasestructure.Table;
import io.vandam.dbdeploy.databasestructure.UniqueKey;

import java.util.ArrayList;
import java.util.Collection;

import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlUniqueKey.getUniqueKey;

class SqlUniqueKeyDiff {
    static Collection<String> diffUniqueKey(final Database source, final Database target) {
        final Collection<String> response = new ArrayList<>();

        // unique key changes
        for (int i = 0; i < source.getTables().size(); i++) {
            final Table sourceTable = source.getTables().get(i);
            final Table targetTable = target.getTable(sourceTable.getSystemName());

            if (null == targetTable) {
                response.addAll(SqlUniqueKey.getUniqueKey(sourceTable.getName(), sourceTable.getUniqueKeys()));

            } else if (!sourceTable.getUniqueKeys().isEmpty()) {
                if (targetTable.getUniqueKeys().isEmpty()) {
                    response.addAll(SqlUniqueKey.getUniqueKey(sourceTable.getName(), sourceTable.getUniqueKeys()));
                } else if (!sourceTable.getUniqueKeys().equals(targetTable.getUniqueKeys())) {
                    for (final UniqueKey uniqueKey : targetTable.getUniqueKeys()) {
                        response.add("ALTER TABLE " + targetTable.getName() + '\n' +
                                "\tDROP UNIQUE " + uniqueKey.getName());
                    }
                    for (final UniqueKey uniqueKey : sourceTable.getUniqueKeys()) {
                        response.add("ALTER TABLE " + targetTable.getName() + '\n' +
                                '\t' + SqlUniqueKey.getUniqueKey(uniqueKey));
                    }
                }
            }
        }
        for (int i = 0; i < target.getTables().size(); i++) {
            final Table targetTable = target.getTables().get(i);

            final Table sourceTable = source.getTable(targetTable.getSystemName());

            if (null != sourceTable) {
                if (!targetTable.getUniqueKeys().isEmpty()) {
                    if (sourceTable.getUniqueKeys().isEmpty()) {
                        for (final UniqueKey uniqueKey : targetTable.getUniqueKeys()) {
                            response.add("ALTER TABLE " + targetTable.getName() + '\n' +
                                    "\tDROP UNIQUE " + uniqueKey.getName());
                        }
                    }
                }
            }
        }

        return response;
    }
}
