package io.vandam.dbdeploy.sql.static_data;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Record")
public class Record {
    @XmlElement(name = "Column")
    private List<Column> m_columns;

    public Collection<Column> getColumns() {
        if (null == m_columns) {
            m_columns = new ArrayList<>();
        }

        return m_columns;
    }

    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }

        if (!Record.class.equals(obj.getClass())) {
            return false;
        }

        final Record other = (Record) obj;
        
        return (null == m_columns) ? (null == other.m_columns) : m_columns.equals(other.m_columns);
    }

    @Override
    public int hashCode() {
        return (null != m_columns) ? m_columns.hashCode() : 0;
    }

    String getValue(final String columnName) {
        for (final Column column : m_columns) {
            if (columnName.equals(column.getColumnName())) {
                return column.getValue();
            }
        }

        return null;
    }
}
