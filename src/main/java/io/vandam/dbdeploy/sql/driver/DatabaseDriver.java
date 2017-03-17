package io.vandam.dbdeploy.sql.driver;

import io.vandam.dbdeploy.sql.procedure.Parameter;
import io.vandam.dbdeploy.configuration.DatabaseConfig;
import io.vandam.dbdeploy.sql.procedure.Direction;
import io.vandam.dbdeploy.sql.procedure.UnknownParameterTypeException;

import java.sql.*;
import java.util.List;

/**
 * The Class Driver.
 */
public class DatabaseDriver {
    /**
     * Commitment control to implement
     */
    private final int m_commitmentControl;

    /**
     * Database config
     */
    private final DatabaseConfig m_databaseConfig;

    /**
     * The Db2 connection.
     */
    private Connection m_dbConnection;

    /**
     * The Db statement.
     */
    private PreparedStatement m_dbPreparedStatement;

    /**
     * The Db statement.
     */
    private Statement m_dbStatement;

    /**
     * The Db callable statement.
     */
    private CallableStatement m_dbCallableStatement;

    /**
     * Instantiates a new driver.
     *
     * @param databaseConfig    Database config
     * @param commitmentControl Commitment control option
     */
    public DatabaseDriver(final DatabaseConfig databaseConfig, final int commitmentControl) {
        m_databaseConfig = databaseConfig;
        m_commitmentControl = commitmentControl;
    }

    /**
     * connect.
     *
     * @return true, if successful
     * @throws ClassNotFoundException the class not found exception
     */
    public boolean connect() throws ClassNotFoundException {

        final String driverName;
        final StringBuilder url = new StringBuilder();

        switch (m_databaseConfig.getDriverType()) {
            case DB2_400:
                driverName = "com.ibm.as400.access.AS400JDBCDriver";

                url.append("jdbc:as400://").append(m_databaseConfig.getHostname()).append(';');
                url.append("user=").append(m_databaseConfig.getUsername()).append(';');
                url.append("password=").append(m_databaseConfig.getPassword()).append(';');
                url.append("database name=").append(m_databaseConfig.getDatabaseName()).append(';');
                url.append("naming=system;");
                url.append("libraries=").append(m_databaseConfig.getSchemaName()).append(';');

                switch (m_commitmentControl) {
                    case Connection.TRANSACTION_READ_UNCOMMITTED:
                        url.append("transaction isolation=read uncomitted;");
                        break;

                    default:
                        url.append("transaction isolation=none;");
                        break;
                }

                url.append("date format=iso;");
                url.append("block size=512;");
                url.append("errors=full;");
                url.append("extended metadata=true");
                break;

            case MYSQL:
                driverName = "com.mysql.jdbc.Driver";

                url.append("jdbc:mysql://").append(m_databaseConfig.getHostname()).append('/').append(m_databaseConfig.getSchemaName()).append('?');
                url.append("user=").append(m_databaseConfig.getUsername()).append('&');
                url.append("password=").append(m_databaseConfig.getPassword()).append('&');
                url.append("useSSL=false");
                break;

            default:
                throw new ClassNotFoundException("Unsupported database driver");
        }

        // Register the driver and connect to the DB
        Class.forName(driverName);

        boolean response = true;
        try {
            m_dbConnection = DriverManager.getConnection(url.toString());

            m_dbConnection.setSchema(m_databaseConfig.getSchemaName());

            if (-1 != m_commitmentControl) {
                switch (m_commitmentControl) {
                    case Connection.TRANSACTION_READ_UNCOMMITTED:
                    case Connection.TRANSACTION_READ_COMMITTED:
                        m_dbConnection.setTransactionIsolation(m_commitmentControl);
                        m_dbConnection.setAutoCommit(false);
                        break;

                    default:
                        m_dbConnection.setTransactionIsolation(m_commitmentControl);
                        break;
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
            response = false;
        }

        return response;
    }

    /**
     * query.
     *
     * @param sql        the sql
     * @param parameters the parameters
     * @return the result set
     * @throws SQLException the SQL exception
     */
    public DatabaseResultSet query(final String sql, final List<String> parameters, final boolean expectResult)
            throws SQLException {
        closeStatement();

        if (null == parameters) {
            m_dbStatement = m_dbConnection.createStatement();

            if (expectResult) {
                return new DatabaseResultSet(m_dbStatement.executeQuery(sql));
            }

            m_dbStatement.execute(sql);
            return null;
        }

        m_dbPreparedStatement = m_dbConnection.prepareStatement(sql);

        for (int i = 0; i < parameters.size(); i++) {
            final String value = parameters.get(i);
            System.out.println(i + ": " + value);
            m_dbPreparedStatement.setString(i + 1, value);
        }

        if (expectResult) {
            return new DatabaseResultSet(m_dbPreparedStatement.executeQuery());
        }

        m_dbPreparedStatement.execute();

        return null;
    }

    /**
     * close statement.
     */
    private void closeStatement() {
        try {
            if (null != m_dbPreparedStatement) {
                m_dbPreparedStatement.close();
                m_dbPreparedStatement = null;
            }

            if (null != m_dbStatement) {
                m_dbStatement.close();
                m_dbStatement = null;
            }

            if (null != m_dbCallableStatement) {
                m_dbCallableStatement.close();
                m_dbCallableStatement = null;
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * close.
     */
    public void close() {
        try {
            if (null != m_dbPreparedStatement) {
                m_dbPreparedStatement.close();
                m_dbPreparedStatement = null;
            }

            if (null != m_dbStatement) {
                m_dbStatement.close();
                m_dbStatement = null;
            }

            if (null != m_dbCallableStatement) {
                m_dbCallableStatement.close();
                m_dbCallableStatement = null;
            }

            if (null != m_dbConnection) {
                m_dbConnection.close();
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * commit.
     *
     * @throws SQLException the SQL exception
     */
    public void commit() throws SQLException {
        m_dbConnection.commit();
    }

    /**
     * rollback.
     *
     * @throws SQLException the SQL exception
     */
    public void rollback() throws SQLException {
        m_dbConnection.rollback();
    }

    public void rollback(final Savepoint savepoint) throws SQLException {
        m_dbConnection.rollback(savepoint);
    }

    public Savepoint setSavePoint(final String name) throws SQLException {
        return m_dbConnection.setSavepoint(name);
    }

    /**
     * call.
     *
     * @param sql        the sql
     * @param parameters the parameters
     * @param values     the values
     * @return the result set
     * @throws SQLException                  the SQL exception
     * @throws UnknownParameterTypeException Unknown parameter type specified
     */
    public DatabaseResultSet call(final String sql, final List<Parameter> parameters, final List<String> values, final boolean expectResult) throws SQLException, UnknownParameterTypeException {
        closeStatement();

        m_dbCallableStatement = m_dbConnection.prepareCall(sql);

        for (int i = 0; i < parameters.size(); i++) {
            if (Direction.IN != parameters.get(i).getDirection()) {
                m_dbCallableStatement.registerOutParameter(i + 1, parameters.get(i).getParameterSqlType());
            }
        }

        for (int i = 0; i < values.size(); i++) {
            m_dbCallableStatement.setString(i + 1, values.get(i));
        }

        ResultSet result = null;
        if (m_dbCallableStatement.execute() && expectResult) {
            result = m_dbCallableStatement.getResultSet();
        }

        for (int i = 0; i < parameters.size(); i++) {
            if (Direction.IN != parameters.get(i).getDirection()) {
                values.add(i, m_dbCallableStatement.getString(i + 1));
            }
        }

        return new DatabaseResultSet(result);
    }
}
