package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import io.vandam.dbdeploy.databasestructure.CheckConstraint;
import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.databasestructure.Table;

import java.util.ArrayList;
import java.util.Collection;

import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlCheckConstraint.getCheckConstraint;

class SqlCheckConstraintDiff {
    static Collection<String> diffCheckConstraint(final Database source, final Database target) {
        final Collection<String> response = new ArrayList<>();

        // check constraint changes
        for (int i = 0; i < source.getTables().size(); i++) {
            final Table sourceTable = source.getTables().get(i);
            final Table targetTable = target.getTable(sourceTable.getSystemName());

            if (null == targetTable) {
                response.addAll(SqlCheckConstraint.getCheckConstraint(sourceTable.getName(), sourceTable.getCheckConstraints()));

            } else if (!sourceTable.getCheckConstraints().isEmpty()) {
                if (targetTable.getCheckConstraints().isEmpty()) {
                    response.addAll(SqlCheckConstraint.getCheckConstraint(sourceTable.getName(), sourceTable.getCheckConstraints()));
                } else if (!sourceTable.getCheckConstraints().equals(targetTable.getCheckConstraints())) {
                    for (final CheckConstraint checkConstraint : targetTable.getCheckConstraints()) {
                        response.add("ALTER TABLE " + targetTable.getName() + '\n' +
                                "\tDROP CHECK " + checkConstraint.getName());
                    }
                    for (final CheckConstraint checkConstraint : sourceTable.getCheckConstraints()) {
                        response.add("ALTER TABLE " + targetTable.getName() + '\n' +
                                '\t' + SqlCheckConstraint.getCheckConstraint(checkConstraint));
                    }
                }
            }
        }
        for (int i = 0; i < target.getTables().size(); i++) {
            final Table targetTable = target.getTables().get(i);

            final Table sourceTable = source.getTable(targetTable.getSystemName());

            if (null != sourceTable) {
                if (!targetTable.getCheckConstraints().isEmpty()) {
                    if (sourceTable.getCheckConstraints().isEmpty()) {
                        for (final CheckConstraint checkConstraint : targetTable.getCheckConstraints()) {
                            response.add("ALTER TABLE " + targetTable.getName() + '\n' +
                                    "\tDROP CHECK " + checkConstraint.getName());
                        }
                    }
                }
            }
        }


        return response;
    }
}
