package io.vandam.dbdeploy.sql;

import io.vandam.dbdeploy.configuration.DatabaseConfig;
import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.databasestructure.UniqueKey;
import io.vandam.dbdeploy.sql.driver.DatabaseDriver;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class ImportDatabaseAbstract.
 */
public abstract class ImportDatabaseAbstract {

    /**
     * The db connection.
     */
    protected DatabaseDriver m_databaseDriver;

    /**
     * The db config
     */
    protected final DatabaseConfig m_databaseConfig;

    /**
     * Instantiates a new i import database.
     *
     * @param databaseConfig the database configuration
     */
    protected ImportDatabaseAbstract(final DatabaseConfig databaseConfig) {
        m_databaseConfig = databaseConfig;
    }

    /**
     * Gets the tables.
     *
     * @param db the db
     * @return the tables
     */
    protected abstract boolean getTables(final Database db);

    /**
     * Gets the columns.
     *
     * @param db the db
     * @return the columns
     */
    protected abstract boolean getColumns(final Database db);

    protected boolean populatePrimaryKeys(final Database database, final String sqlQuery) {
        boolean resp = true;

        int i = 0;
        while (resp && (i < database.getTables().size())) {
            final List<String> parameters = new ArrayList<>();
            parameters.add(m_databaseConfig.getSchemaName());
            parameters.add(database.getTables().get(i).getName());

            try {
                final ResultSet resultSet = m_databaseDriver.query(sqlQuery, parameters, true).getResultSet();
                if (null != resultSet) {
                    while (resultSet.next()) {
                        System.out.println("On " + database.getTables().get(i).getName() + " found primary key column "
                                + resultSet.getString("COLUMN_NAME"));

                        database.getTables().get(i).getPrimaryKey().add(resultSet.getString("COLUMN_NAME"));
                    }

                    resultSet.close();
                }
            } catch (final Exception e) {
                e.printStackTrace();
                resp = false;
            }

            i++;
        }

        return resp;
    }

    /**
     * Gets the primary keys.
     *
     * @param db the db
     * @return the primary keys
     */
    protected abstract boolean getPrimaryKeys(final Database db);

    /**
     * Gets the check constraints.
     *
     * @param db the db
     * @return the check constraints
     */
    protected abstract boolean getCheckConstraints(final Database db);

    /**
     * Gets the foreign keys.
     *
     * @param db the db
     * @return the foreign keys
     */
    protected abstract boolean getForeignKeys(final Database db);

    protected boolean populateUniqueKeys(final Database db, final String sql) {
        boolean resp = true;

        int i = 0;
        while (resp && (i < db.getTables().size())) {
            final List<String> parameters = new ArrayList<>();
            parameters.add(m_databaseConfig.getSchemaName());
            parameters.add(db.getTables().get(i).getName());

            try {
                final ResultSet res = m_databaseDriver.query(sql, parameters, true).getResultSet();
                if (null != res) {
                    String pConstraint = "";
                    UniqueKey uk = null;

                    while (res.next()) {
                        final String constraintName = res.getString("CONSTRAINT_NAME");

                        if (!pConstraint.equals(constraintName)) {
                            if (!pConstraint.isEmpty()) {
                                db.getTables().get(i).getUniqueKeys().add(uk);
                            }

                            pConstraint = constraintName;
                            uk = new UniqueKey();
                            uk.setName(constraintName);
                            System.out.println(
                                    "On " + db.getTables().get(i).getName() + " found unique key " + constraintName);
                        }

                        System.out.println("On " + db.getTables().get(i).getName() + " found unique key column "
                                + res.getString("COLUMN_NAME") + " on " + pConstraint);

                        if (null != uk) {
                            uk.getColumns().add(res.getString("COLUMN_NAME"));
                        }
                    }

                    if (!pConstraint.isEmpty()) {
                        db.getTables().get(i).getUniqueKeys().add(uk);
                    }

                    res.close();
                }
            } catch (final Exception e) {
                e.printStackTrace();
                resp = false;
            }

            i++;
        }

        return resp;
    }

    /**
     * Gets the unique keys.
     *
     * @param db the db
     * @return the unique keys
     */
    protected abstract boolean getUniqueKeys(final Database db);

    /**
     * Gets the indexes.
     *
     * @param db the db
     * @return the indexes
     */
    protected abstract boolean getIndexes(final Database db);

    /**
     * Gets the database.
     *
     * @return the database
     * @throws ClassNotFoundException the class not found exception
     */
    public abstract Database getDatabase() throws ClassNotFoundException;
}
