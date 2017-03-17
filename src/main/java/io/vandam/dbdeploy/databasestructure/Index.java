package io.vandam.dbdeploy.databasestructure;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class Index.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "index")
public class Index {

    /**
     * The name.
     */
    @XmlAttribute(name = "name", required = true)
    private String name;

    /**
     * The system name.
     */
    @XmlAttribute(name = "systemName", required = true)
    private String systemName;

    /**
     * The description.
     */
    @XmlAttribute(name = "description", required = true)
    private String description;

    /**
     * The columns.
     */
    private List<String> column;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param value the name to set
     * @throws InvalidLengthException the invalid length exception
     */
    public void setName(final String value) throws InvalidLengthException {
        if ((1 > value.trim().length()) || (50 < value.trim().length())) {
            throw new InvalidLengthException("Name should be between 1 and 50 characters");
        }
        name = value.trim();
    }

    /**
     * Gets the system name.
     *
     * @return the systemName
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Sets the system name.
     *
     * @param value the new system name
     * @throws InvalidLengthException the invalid length exception
     */
    public void setSystemName(final String value) throws InvalidLengthException {
        if (10 < value.trim().length()) {
            throw new InvalidLengthException("System Name should be between 1 and 10 characters");
        }
        systemName = value.trim();
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public CharSequence getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param value the new description
     * @throws InvalidLengthException the invalid length exception
     */
    public void setDescription(final String value) throws InvalidLengthException {
        if ((null != value) && (50 < value.trim().length())) {
            throw new InvalidLengthException("Description should be between 1 and 50 characters");
        }
        description = (null == value) ? "" : value.trim();
    }

    /**
     * Gets the columns.
     *
     * @return the columns
     */
    public List<String> getColumns() {
        if (null == column) {
            column = new ArrayList<>();
        }
        return column;
    }

    /**
     * Set columns
     *
     * @param value Columns
     */
    public void setColumns(final List<String> value) {
        column = value;
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

        if (!Index.class.equals(obj.getClass())) {
            return false;
        }

        final Index other = (Index) obj;

        boolean resp = (null == name) ? (null == other.name) : name.equals(other.name);
        resp &= (null == systemName) ? (null == other.systemName) : systemName.equals(other.systemName);
        resp &= (null == description) ? (null == other.description) : description.equals(other.description);
        resp &= (null == column) ? (null == other.column) : column.equals(other.column);
        return resp;
    }

    @Override
    public int hashCode() {
        int result = (null != name) ? name.hashCode() : 0;
        result = (31 * result) + ((null != systemName) ? systemName.hashCode() : 0);
        result = (31 * result) + ((null != description) ? description.hashCode() : 0);
        result = (31 * result) + ((null != column) ? column.hashCode() : 0);
        return result;
    }
}
