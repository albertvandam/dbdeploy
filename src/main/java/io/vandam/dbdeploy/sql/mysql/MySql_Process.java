package io.vandam.dbdeploy.sql.mysql;

import io.vandam.dbdeploy.sql.IProcess;
import io.vandam.dbdeploy.sql.db2_400.InvalidIdentityColumnException;
import io.vandam.dbdeploy.configuration.DatabaseConfig;
import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.sql.db2_400.InvalidDefaultValueException;
import io.vandam.dbdeploy.sql.driver.DriverType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySql_Process implements IProcess {

    /**
     * Import my sql database.
     *
     * @param config the config
     * @return the database
     * @throws ClassNotFoundException the class not found exception
     */
    @Override
    public Database importDatabase(final DatabaseConfig config) throws ClassNotFoundException {
        final ImportDatabase mysql_ids = new ImportDatabase(config);
        return mysql_ids.getDatabase();
    }

    @Override
    public void importStoredProcedures(final DatabaseConfig config, final String targetDirectory) throws SQLException, IOException {
    }

    @Override
    public void importTriggers(final DatabaseConfig config, final String targetDirectory) throws IOException, SQLException {
    }

    @Override
    public List<String> getChangeSql(final Database sourceDatabase, final Database targetDatabase) throws InvalidIdentityColumnException, InvalidDefaultValueException {
        return new ArrayList<>();
    }

//    @Override
//    public List<String> getCreateSql(final Database database) throws InvalidIdentityColumnException, InvalidDefaultValueException {
//        return new ArrayList<>();
//    }

    @Override
    public DriverType getType() {
        return DriverType.MYSQL;
    }
}
