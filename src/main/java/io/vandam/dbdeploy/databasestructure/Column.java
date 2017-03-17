package io.vandam.dbdeploy.databasestructure;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.text.DecimalFormat;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "column")
public final class Column {

    /**
     * The name.
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * The system name.
     */
    @XmlAttribute(name = "systemName", required = true)
    private String m_systemName;

    /**
     * The description.
     */
    @XmlAttribute(name = "description", required = true)
    private String m_description;

    /**
     * The type.
     */
    @XmlAttribute(name = "type", required = true)
    private ColumnType m_type;

    /**
     * The size.
     */
    @XmlAttribute(name = "size", required = true)
    private double m_size;

    /**
     * Field can be null
     */
    @XmlAttribute(name = "canBeNull", required = true)
    private boolean m_canBeNull;

    /**
     * The default.
     */
    @XmlAttribute(name = "default")
    private String m_defaultValue;
    /**
     * The identity column.
     */
    @XmlAttribute(name = "identity")
    private boolean m_identity;

    /**
     * Instantiates a new column.
     */
    public Column() {
        // left for JAXB
    }

    /**
     * Instantiates a new column.
     *
     * @param name         the m_name
     * @param systemName   the system m_name
     * @param description  the m_description
     * @param type         the m_type
     * @param size         the m_size
     * @param canBeNull     the m_canBeNull
     * @param defaultValue the default value
     * @param isIdentity     the m_identity
     */
    public Column(final String name, final String systemName, final String description, final ColumnType type,
                  final double size, final boolean canBeNull, final String defaultValue, final boolean isIdentity) {
        setName(name);
        setSystemName(systemName);
        setDescription(description);
        m_type = type;
        m_size = size;
        m_canBeNull = canBeNull;
        m_defaultValue = defaultValue;
        m_identity = isIdentity;
    }

    /**
     * @return the m_defaultValue
     */
    public String getDefaultValue() {
        return m_defaultValue;
    }

    /**
     * Sets the default value.
     *
     * @param value the m_defaultValue to set
     */
    public void setDefaultValue(final String value) {
        m_defaultValue = value;
    }

    public void setDefaultValue(final Double value) {
        final DecimalFormat formatter = new DecimalFormat("###################0.00000");

        m_defaultValue = formatter.format(value);
    }

    public void setDefaultValue(final int value) {
        final DecimalFormat formatter = new DecimalFormat("###################0");

        m_defaultValue = formatter.format(value);
    }

    /**
     * Gets the value of the m_name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the value of the m_name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        m_name = value.trim();
    }

    /**
     * Gets the value of the m_systemName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSystemName() {
        return m_systemName;
    }

    /**
     * Sets the value of the m_systemName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSystemName(final String value) {
        m_systemName = value.trim();
    }

    /**
     * Gets the value of the m_description property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDescription() {
        return m_description;
    }

    /**
     * Sets the value of the m_description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(final String value) {
        m_description = (null == value) ? "" : value.trim();
    }

    /**
     * Gets the value of the m_type property.
     *
     * @return possible object is
     * {@link ColumnType }
     */
    public ColumnType getType() {
        return m_type;
    }

    /**
     * Sets the value of the m_type property.
     *
     * @param value allowed object is
     *              {@link ColumnType }
     */
    public void setType(final ColumnType value) {
        m_type = value;
    }

    /**
     * Gets the value of the m_size property.
     *
     * @return the m_size
     */
    public double getSize() {
        return m_size;
    }

    /**
     * Sets the value of the m_size property.
     *
     * @param value the new m_size
     */
    public void setSize(final double value) {
        m_size = value;
    }

    /**
     * Gets the m_size.
     *
     * @param format the format
     * @return the m_size
     */
    public String getSize(final String format) {
        final DecimalFormat formatter = new DecimalFormat(format);

        return formatter.format(m_size);
    }

    /**
     * Gets the value of the m_canBeNull property.
     *
     * @return true, if is m_canBeNull
     */
    public boolean canBeNull() {
        return m_canBeNull;
    }

    /**
     * Sets the value of the m_canBeNull property.
     *
     * @param value the new m_canBeNull
     */
    void setCanBeNull(final boolean value) {
        m_canBeNull = value;
    }

    /**
     * Gets the value of the m_identity property.
     *
     * @return true, if is m_identity
     */
    public boolean isIdentity() {
        return m_identity;
    }

    /**
     * Sets the value of the m_identity property.
     *
     * @param value the new m_identity
     */
    void setIdentity(final boolean value) {
        m_identity = value;
    }

    /**
     * Equals.
     *
     * @param obj the other
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }

        if (!Column.class.equals(obj.getClass())) {
            return false;
        }

        final Column other = (Column) obj;

        boolean resp = (null == m_name) ? (null == other.m_name) : m_name.equals(other.m_name);
        resp &= (null == m_systemName) ? (null == other.m_systemName) : m_systemName.equals(other.m_systemName);
        resp &= (null == m_description) ? (null == other.m_defaultValue) : m_description.equals(other.m_description);
        resp &= m_type == other.m_type;
        resp &= m_size == other.m_size;
        resp &= m_canBeNull == other.m_canBeNull;
        resp &= (null == m_defaultValue) ? (null == other.m_defaultValue) : m_defaultValue.equals(other.m_defaultValue);
        resp &= m_identity == other.m_identity;
        return resp;
    }

    @Override
    public int hashCode() {
        int result = (null != m_name) ? m_name.hashCode() : 0;
        result = (31 * result) + ((null != m_systemName) ? m_systemName.hashCode() : 0);
        result = (31 * result) + ((null != m_description) ? m_description.hashCode() : 0);
        result = (31 * result) + ((null != m_type) ? m_type.hashCode() : 0);

        final long temp = Double.doubleToLongBits(m_size);
        result = (31 * result) + (int) (temp ^ (temp >>> 32));

        result = (31 * result) + (m_canBeNull ? 1 : 0);
        result = (31 * result) + ((null != m_defaultValue) ? m_defaultValue.hashCode() : 0);
        result = (31 * result) + (m_identity ? 1 : 0);
        return result;
    }
}
