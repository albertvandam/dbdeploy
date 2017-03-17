package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import io.vandam.dbdeploy.databasestructure.CheckConstraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class SqlCheckConstraint {
    static String getCheckConstraint(final CheckConstraint checkConstraint) {
        return "ADD CONSTRAINT " + checkConstraint.getName() + " CHECK (\n" +
                "\t\t" + checkConstraint.getCheck() + '\n' +
                "\t)";
    }

    static List<String> getCheckConstraint(final String tableName, final Collection<CheckConstraint> checkConstraints) {
        final List<String> response = new ArrayList<>();

        if (!checkConstraints.isEmpty()) {
            for (final CheckConstraint checkConstraint : checkConstraints) {
                response.add("ALTER TABLE " + tableName + "\n\t" + getCheckConstraint(checkConstraint));
            }
        }

        return response;
    }
}
