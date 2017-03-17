package io.vandam.dbdeploy.sql.static_data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class WhereClauseBuilder {
    private final List<String> m_parameters;
    private String m_sql;
    private String m_displaySql;

    WhereClauseBuilder() {
        m_sql = "";
        m_displaySql = "";
        m_parameters = new ArrayList<>();
    }

    String getSql() {
        return m_sql;
    }

    String getDisplaySql() {
        return m_displaySql;
    }

    Collection<String> getParameters() {
        return m_parameters;
    }

    void build(final Iterable<String> primaryKey, final Record record) {
        final StringBuilder sql = new StringBuilder();
        final StringBuilder displaySql = new StringBuilder();

        boolean first = true;
        for (final String column : primaryKey) {
            if (first) {
                first = false;
            } else {
                sql.append(" AND ");
                displaySql.append(" AND ");
            }
            sql.append(column).append("=?");
            displaySql.append(column).append("='").append(record.getValue(column)).append('\'');
            m_parameters.add(record.getValue(column));
        }

        m_sql = sql.toString();
        m_displaySql = displaySql.toString();
    }
}
