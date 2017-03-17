package io.vandam.dbdeploy.sql.static_data;

import io.vandam.dbdeploy.databasestructure.Database;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "StaticData")
@XmlAccessorType(XmlAccessType.FIELD)
public class StaticData {
    @XmlAttribute(name = "tableName")
    private String m_tableName;

    @XmlElement(name = "Record")
    private List<Record> m_records;
    
    @XmlElement(name = "AdditionalData")
    private List<AdditionalData> m_additionalData;

    @XmlTransient
    private List<String> m_recordKeys;

    @XmlElement
    private List<String> m_primaryKey;

    /**
     * From xml.
     *
     * @param filename the filename
     * @return the StaticData
     * @throws JAXBException the JAXB exception
     */
    public static StaticData fromXml(final String filename) throws JAXBException {
        final File file = new File(filename);
        final JAXBContext jaxbContext = JAXBContext.newInstance(StaticData.class);

        final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (StaticData) jaxbUnmarshaller.unmarshal(file);
    }
    
    public List<AdditionalData> getAdditionalData()
    {
    	if (null == m_additionalData) {
    		m_additionalData = new ArrayList<>();
    	}
    	
    	return m_additionalData;
    }

    private void initRecords() throws Exception {
        if (null == m_recordKeys) {
            m_recordKeys = new ArrayList<>();
        }

        if (null == m_records) {
            m_records = new ArrayList<>();
        }

        if (m_recordKeys.size() != m_records.size()) {
            m_recordKeys.clear();

            if (!m_records.isEmpty()) {
            	if (null == m_tableName) {
            		throw new Exception("No table name specified in data configuration");
            	}
            	
                if ((null == m_primaryKey) || m_primaryKey.isEmpty()) {
                    m_primaryKey = Database.getPrimaryKey(m_tableName);
                }
                
                if (null == m_primaryKey) {
                	throw new Exception("No primary key defined");
                }

                for (final Record record : m_records) {
                    final StringBuilder key = new StringBuilder();

                    for (final Column column : record.getColumns()) {
                        if (m_primaryKey.contains(column.getColumnName())) {
                            key.append(column.getValue());
                        }
                    }

                    m_recordKeys.add(key.toString());
                }
            }
        }
    }

    public List<Record> getRecords() {
        if (null == m_records) {
            m_records = new ArrayList<>();
        }

        return m_records;
    }

    public Record getRecord(final String key) throws Exception {
        return m_records.get(getKeys().indexOf(key));
    }

    public boolean containsRecord(final String key) throws Exception {
        return getKeys().contains(key);
    }

    public List<String> getKeys() throws Exception {
        initRecords();

        return m_recordKeys;
    }

    public void putRecord(final String key, final Record record) throws Exception {
    	if (null == m_recordKeys) {
    		initRecords();
    	}
    	
        if (m_recordKeys.contains(key)) {
            throw new Exception("Duplicate key " + key);
        }

        m_recordKeys.add(key);
        m_records.add(m_recordKeys.indexOf(key), record);
    }

    /**
     * To xml.
     *
     * @param filename the filename
     * @throws JAXBException the JAXB exception
     */
    public void toXml(final String filename) throws JAXBException {
        final File file = new File(filename);
        final JAXBContext jaxbContext = JAXBContext.newInstance(StaticData.class);
        final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        jaxbMarshaller.marshal(this, file);
    }

    public String getTableName() {
        return m_tableName;
    }

    public void setTableName(final String tableName) {
        m_tableName = tableName;
    }


    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }

        if (!StaticData.class.equals(obj.getClass())) {
            return false;
        }

        final StaticData other = (StaticData) obj;
        
        boolean resp = (null == m_tableName) ? (null == other.m_tableName) : m_tableName.equals(other.m_tableName);
        resp &= (null == m_records) ? (null == other.m_records) : m_records.equals(other.m_records);
        return resp;
    }

    @Override
    public int hashCode() {
        int result = (null != m_tableName) ? m_tableName.hashCode() : 0;
        result = (31 * result) + ((null != m_records) ? m_records.hashCode() : 0);
        return result;
    }
}
