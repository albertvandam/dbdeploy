package io.vandam.dbdeploy.sql.driver;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseResultSet {
    /**
     * Result set
     */
    private final ResultSet m_resultSet;

    DatabaseResultSet(final ResultSet resultSet) {
        m_resultSet = resultSet;
    }

    List<Record> fetchAll() throws SQLException {
        if (null == m_resultSet) {
            return null;
        }

        final List<Record> response = new ArrayList<>();

        final ResultSetMetaData metaData = m_resultSet.getMetaData();

        final List<String> columnList = new ArrayList<>();
        final int columnCount = metaData.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            columnList.add(i, metaData.getColumnName(i+1));
        }

        while (m_resultSet.next()) {
            final Record record = new Record();

            for (final String columnName : columnList) {
                record.put(columnName, m_resultSet.getString(columnName));
            }

            response.add(record);
        }

        m_resultSet.close();

        return response;
    }

    boolean next() throws SQLException {
        return m_resultSet.next();
    }

    String getString(final String columnLabel) throws SQLException {
        return m_resultSet.getString(columnLabel);
    }

    public void close() throws SQLException {
        m_resultSet.close();
    }

    public ResultSet getResultSet() {
        return m_resultSet;
    }
}
