package io.vandam.dbdeploy.sql;

import io.vandam.dbdeploy.configuration.DatabaseConfig;
import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.sql.db2_400.InvalidDefaultValueException;
import io.vandam.dbdeploy.sql.db2_400.InvalidIdentityColumnException;
import io.vandam.dbdeploy.sql.driver.DriverType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Process abstract class
 */
public interface IProcess {

    /**
     * Import database
     *
     * @param config Database configuration
     * @return Database structure
     * @throws ClassNotFoundException
     */
    Database importDatabase(final DatabaseConfig config) throws ClassNotFoundException;

    void importStoredProcedures(final DatabaseConfig config, final String targetDirectory) throws SQLException, IOException, ClassNotFoundException;

    void importTriggers(final DatabaseConfig config, final String targetDirectory) throws IOException, SQLException, ClassNotFoundException;

    List<String> getChangeSql(final Database sourceDatabase, final Database targetDatabase) throws InvalidIdentityColumnException, InvalidDefaultValueException;

//    List<String> getCreateSql(final Database database) throws InvalidIdentityColumnException, InvalidDefaultValueException;

    DriverType getType();
}
