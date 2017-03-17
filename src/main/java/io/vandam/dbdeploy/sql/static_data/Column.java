package io.vandam.dbdeploy.sql.static_data;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Column")
public class Column {
    @XmlAttribute(name = "name", required = true)
    private String m_columnName;

    @XmlValue
    private String m_value;

    /**
     * Constructor - left for JAX-B
     */
    public Column() {
        // kept for JAX-B
    }

    public Column(final String columnName, final String value) {
        m_columnName = columnName;
        m_value = value;
    }

    String getColumnName() {
        return m_columnName;
    }

//    public void setColumnName(final String columnName) {
//        m_columnName = columnName;
//    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        m_value = value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }

        if (!Column.class.equals(obj.getClass())) {
            return false;
        }

        final Column other = (Column) obj;
        
        boolean resp = (null == m_columnName) ? (null == other.m_columnName) : m_columnName.equals(other.m_columnName);
        resp &= (null == m_value) ? (null == other.m_value) : m_value.equals(other.m_value);
        return resp;
    }

    @Override
    public int hashCode() {
        int result = (null != m_columnName) ? m_columnName.hashCode() : 0;
        result = (31 * result) + ((null != m_value) ? m_value.hashCode() : 0);
        return result;
    }
}
