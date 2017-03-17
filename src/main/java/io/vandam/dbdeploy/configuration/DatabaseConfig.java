package io.vandam.dbdeploy.configuration;

import io.vandam.dbdeploy.sql.driver.DriverType;

import javax.xml.bind.annotation.*;

/**
 * The Class Database.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"m_name", "m_driverType", "m_hostname", "m_username", "m_password", "m_databaseName", "m_schemaName"})
public class DatabaseConfig {
    @XmlAttribute(name="identifier")
    private String m_name;

    /**
     * The driver.
     */
    @XmlAttribute(name = "driverType")
    private DriverType m_driverType;

    /**
     * The host.
     */
    @XmlAttribute(name = "hostname")
    private String m_hostname;

    /**
     * The user.
     */
    @XmlAttribute(name = "username")
    private String m_username;

    /**
     * The password.
     */
    @XmlAttribute(name = "password")
    private String m_password;

    /**
     * The database.
     */
    @XmlAttribute(name = "databaseName")
    private String m_databaseName;

    /**
     * The schema.
     */
    @XmlAttribute(name = "schemaName")
    private String m_schemaName;

    /**
     * Constructor
     */
    public DatabaseConfig() {
        // Kept for JAXB
    }

    /**
     * @param driverType   Driver type
     * @param hostname     Hostname
     * @param username     Username
     * @param password     Password
     * @param databaseName Database name
     * @param schemaName   Schema name
     */
    public DatabaseConfig(final String name, final DriverType driverType, final String hostname, final String username, final String password, final String databaseName, final String schemaName) {
        m_name = name;
        m_driverType = driverType;
        m_hostname = hostname;
        m_username = username;
        m_password = password;
        m_databaseName = databaseName;
        m_schemaName = schemaName;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHostname() {
        return m_hostname;
    }

    /**
     * Sets the host.
     *
     * @param value the host to set
     */
    void setHostname(final String value) {
        m_hostname = value;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public String getUsername() {
        return m_username;
    }

    /**
     * Sets the user.
     *
     * @param value the user to set
     */
    void setUsername(final String value) {
        m_username = value;
    }

    /**
     * Gets the m_password.
     *
     * @return the m_password
     */
    public String getPassword() {
        return m_password;
    }

    /**
     * Sets the m_password.
     *
     * @param value the m_password to set
     */
    void setPassword(final String value) {
        m_password = value;
    }

    /**
     * Gets the database.
     *
     * @return the database
     */
    public String getDatabaseName() {
        return m_databaseName;
    }

    /**
     * Sets the database.
     *
     * @param value the database to set
     */
    void setDatabaseName(final String value) {
        m_databaseName = value;
    }

    /**
     * Gets the schema.
     *
     * @return the schema
     */
    public String getSchemaName() {
        return m_schemaName;
    }

    /**
     * Sets the schema.
     *
     * @param value the schema to set
     */
    void setSchemaName(final String value) {
        m_schemaName = value;
    }

    /**
     * Gets the driver.
     *
     * @return the driver
     */
    public DriverType getDriverType() {
        return m_driverType;
    }

    /**
     * Sets the driver.
     *
     * @param value the new driver
     */
    void setDriverType(final DriverType value) {
        m_driverType = value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }

        if (!DatabaseConfig.class.equals(obj.getClass())) {
            return false;
        }

        final DatabaseConfig other = (DatabaseConfig) obj;

        boolean response = (null == m_databaseName) ? (null == other.m_databaseName) : m_databaseName.equals(other.m_databaseName);
        response &= m_driverType == other.m_driverType;
        response &= (null == m_hostname) ? (null == other.m_hostname) : m_hostname.equals(other.m_hostname);
        response &= (null == m_password) ? (null == other.m_password) : m_password.equals(other.m_password);
        response &= (null == m_schemaName) ? (null == other.m_schemaName) : m_schemaName.equals(other.m_schemaName);
        response &= (null == m_username) ? (null == other.m_username) : m_username.equals(other.m_username);
        return response;
    }

    @Override
    public int hashCode() {
        int result = (null != m_driverType) ? m_driverType.hashCode() : 0;
        result = (31 * result) + ((null != m_hostname) ? m_hostname.hashCode() : 0);
        result = (31 * result) + ((null != m_username) ? m_username.hashCode() : 0);
        result = (31 * result) + ((null != m_password) ? m_password.hashCode() : 0);
        result = (31 * result) + ((null != m_databaseName) ? m_databaseName.hashCode() : 0);
        result = (31 * result) + ((null != m_schemaName) ? m_schemaName.hashCode() : 0);
        return result;
    }
}
