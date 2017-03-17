package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.databasestructure.Index;
import io.vandam.dbdeploy.databasestructure.Table;

import java.util.ArrayList;
import java.util.Collection;

import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlIndex.getIndex;

class SqlIndexDiff {
    static Collection<String> diffIndex(final Database source, final Database target) {
        final Collection<String> response = new ArrayList<>();

        // index changes
        for (int i = 0; i < source.getTables().size(); i++) {
            final Table sourceTable = source.getTables().get(i);
            final Table targetTable = target.getTable(sourceTable.getSystemName());

            if (null == targetTable) {
                response.addAll(SqlIndex.getIndex(sourceTable.getName(), sourceTable.getIndexes()));

            } else if (!sourceTable.getIndexes().isEmpty()) {
                if (targetTable.getIndexes().isEmpty()) {
                    response.addAll(SqlIndex.getIndex(sourceTable.getName(), sourceTable.getIndexes()));
                } else if (!sourceTable.getIndexes().equals(targetTable.getIndexes())) {
                    for (final Index index : targetTable.getIndexes()) {
                        response.add("DROP INDEX " + index.getName());
                    }
                    for (final Index index : sourceTable.getIndexes()) {
                        response.addAll(SqlIndex.getIndex(sourceTable.getName(), index));
                    }
                }
            }
        }
        for (int i = 0; i < target.getTables().size(); i++) {
            final Table targetTable = target.getTables().get(i);

            final Table sourceTable = source.getTable(targetTable.getSystemName());

            if (null != sourceTable) {
                if (!targetTable.getIndexes().isEmpty()) {
                    if (sourceTable.getIndexes().isEmpty()) {
                        for (final Index index : targetTable.getIndexes()) {
                            response.add("DROP INDEX " + index.getName());
                        }
                    }
                }
            }
        }

        return response;
    }
}
