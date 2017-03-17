package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import io.vandam.dbdeploy.sql.ISqlGeneration;
import io.vandam.dbdeploy.sql.db2_400.InvalidIdentityColumnException;
import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.databasestructure.Table;
import io.vandam.dbdeploy.sql.db2_400.InvalidDefaultValueException;

import java.util.ArrayList;
import java.util.List;

import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlCheckConstraint.getCheckConstraint;
import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlForeignKey.getForeignKey;
import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlIndex.getIndex;
import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlTable.getTable;
import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlTableDiff.diffTable;
import static io.vandam.dbdeploy.sql.db2_400.sql_generation.SqlUniqueKey.getUniqueKey;

public class SqlGeneration implements ISqlGeneration {
    @Override
    public List<String> getDatabase(final Database db) throws InvalidIdentityColumnException, InvalidDefaultValueException {
        final List<String> response = new ArrayList<>();

        // tables
        for (int i = 0; i < db.getTables().size(); i++) {
            final Table tbl = db.getTables().get(i);

            response.addAll(getTable(tbl));
        }

        // unique keys
        for (int i = 0; i < db.getTables().size(); i++) {
            final Table tbl = db.getTables().get(i);

            response.addAll(getUniqueKey(tbl.getName(), tbl.getUniqueKeys()));
        }

        // foreign keys
        for (int i = 0; i < db.getTables().size(); i++) {
            final Table tbl = db.getTables().get(i);

            response.addAll(getForeignKey(tbl.getName(), tbl.getForeignKeys()));
        }

        // check constraints
        for (int i = 0; i < db.getTables().size(); i++) {
            final Table tbl = db.getTables().get(i);

            response.addAll(getCheckConstraint(tbl.getName(), tbl.getCheckConstraints()));
        }

        // indexes
        for (int i = 0; i < db.getTables().size(); i++) {
            final Table tbl = db.getTables().get(i);

            response.addAll(getIndex(tbl.getName(), tbl.getIndexes()));
        }

        return response;
    }

    @Override
    public List<String> getDiffDatabase(final Database source, final Database target) throws InvalidIdentityColumnException, InvalidDefaultValueException {
        final List<String> response = new ArrayList<>();

        response.addAll(diffTable(source, target));
        response.addAll(SqlUniqueKeyDiff.diffUniqueKey(source, target));
        response.addAll(SqlForeignKeyDiff.diffForeignKey(source, target));
        response.addAll(SqlCheckConstraintDiff.diffCheckConstraint(source, target));
        response.addAll(SqlIndexDiff.diffIndex(source, target));

        return response;
    }
}
