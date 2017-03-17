package io.vandam.dbdeploy.configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class Configuration.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@XmlType(name = "Configuration", propOrder = "m_source")
public class Configuration {

    /**
     * The m_source.
     */
    @XmlElement(name="datasource", required = true)
    private List<DatabaseConfig> m_source;

    /**
     * From xml.
     *
     * @param filename the filename
     * @return the configuration
     * @throws JAXBException the JAXB exception
     */
    public static Configuration fromXml(final String filename) throws JAXBException {
        final File file = new File(filename);
        final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (Configuration) jaxbUnmarshaller.unmarshal(file);
    }

    /**
     * Sets the source.
     *
     * @param value the new m_source
     */
    void setConfig(final DatabaseConfig value) {
        if (null == m_source) {
            m_source = new ArrayList<>();
        }

        if (m_source.contains(value)) {
            m_source.remove(value);
        }

        m_source.add(value);
    }

    /**
     * Gets the configuration
     *
     * @param key the key
     * @return the m_source
     */
    public DatabaseConfig getConfig(final String key) {
        if (null == m_source) {
            m_source = new ArrayList<>();
        }

        for (final DatabaseConfig databaseConfig : m_source) {
            if (key.equals(databaseConfig.getName())) {
                return databaseConfig;
            }
        }

        return null;
    }

    /**
     * To xml.
     *
     * @param filename the filename
     * @throws JAXBException the JAXB exception
     */
    void toXml(final String filename) throws JAXBException {
        final File file = new File(filename);
        final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
        final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        jaxbMarshaller.marshal(this, file);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj Object to compare
     * @return true if equal
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (null == obj) {
            return false;
        }

        if (!Configuration.class.equals(obj.getClass())) {
            return false;
        }

        final Configuration other = (Configuration) obj;

        return (null == m_source) ? (null == other.m_source) : m_source.equals(other.m_source);
    }

    /**
     * Returns a hash code value for the object.
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return (null != m_source) ? m_source.hashCode() : 0;
    }

    void removeConfig(final String source) {
        if (null != m_source) {
            for (final DatabaseConfig databaseConfig : m_source) {
                if (source.equals(databaseConfig.getName())) {
                    m_source.remove(databaseConfig);
                }
            }
        }
    }
}
