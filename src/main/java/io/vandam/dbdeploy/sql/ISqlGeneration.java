package io.vandam.dbdeploy.sql;

import io.vandam.dbdeploy.sql.db2_400.InvalidIdentityColumnException;
import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.sql.db2_400.InvalidDefaultValueException;

import java.util.List;

/**
 * The Interface IExportDatabase.
 */
public interface ISqlGeneration {
    /**
     * Gets the database sql.
     *
     * @param db the db
     * @return the database sql
     */
     List<String> getDatabase(final Database db) throws InvalidIdentityColumnException, InvalidDefaultValueException;

    /**
     * Gets the diff database sql.
     *
     * @param source the source
     * @param target the target
     * @return the diff database sql
     */
     List<String> getDiffDatabase(final Database source, final Database target) throws InvalidIdentityColumnException, InvalidDefaultValueException;
}
