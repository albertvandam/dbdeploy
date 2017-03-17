package io.vandam.dbdeploy.sql.db2_400;

import io.vandam.dbdeploy.sql.driver.DatabaseDriver;
import io.vandam.dbdeploy.sql.driver.DriverType;
import io.vandam.dbdeploy.sql.static_data.Column;
import io.vandam.dbdeploy.configuration.DatabaseConfig;
import io.vandam.dbdeploy.databasestructure.Table;
import io.vandam.dbdeploy.sql.static_data.Record;
import io.vandam.dbdeploy.sql.static_data.StaticData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class ImportStaticData {
    private final DatabaseConfig m_databaseConfig;

    public ImportStaticData(final DatabaseConfig databaseConfig) {
        m_databaseConfig = databaseConfig;
    }

    public void getStaticData(final Table table, final String targetDirectory) throws Exception {
        final StaticData response = new StaticData();
        response.setTableName(table.getName());

        final int commitmentControl = (m_databaseConfig.getDriverType() == DriverType.MYSQL) ? -1 : Connection.TRANSACTION_NONE;

        final DatabaseDriver m_databaseDriver = new DatabaseDriver(m_databaseConfig, commitmentControl);

        m_databaseDriver.connect();

        final StringBuilder sort = new StringBuilder();
        for (final String column : table.getPrimaryKey()) {
            if (0 != sort.length()) {
                sort.append(", ");
            }
            sort.append(column);
        }

        final StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ").append(table.getName());
        if (0 != sort.length()) {
            query.append(" ORDER BY ").append(sort);
        }

        final ResultSet resultSet = m_databaseDriver.query(query.toString(), null, true).getResultSet();

        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int colCount = metaData.getColumnCount();

        final List<String> columnNames = new ArrayList<>();
        for (int i = 0; i < colCount; i++) {
            if (!table.getIgnoredImportColumns().contains(metaData.getColumnLabel(i + 1))) {
                columnNames.add(metaData.getColumnLabel(i + 1));
            }
        }

        while (resultSet.next()) {
            final Record record = new Record();
            final StringBuilder key = new StringBuilder();

            for (final String columnName : columnNames) {
                String value = resultSet.getString(columnName);
                value = (null == value) ? "null" : value.trim();

                if ("AUTSETTB".equals(table.getName()) && "FUNCTIONID".equals(columnName) && value.isEmpty()) {
                    value = "null";
                }

                final Column column = new Column(columnName, value);

                record.getColumns().add(column);

                if (table.getPrimaryKey().contains(columnName)) {
                    key.append(value);
                }
            }

            response.putRecord(key.toString(), record);
        }

        m_databaseDriver.close();

        response.toXml(targetDirectory + table.getName() + ".xml");
    }
}
