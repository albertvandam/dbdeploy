package io.vandam.dbdeploy.test;

import io.vandam.dbdeploy.test.definition.TestDefinition;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestConfiguration", propOrder = {"m_description", "m_enabled", "m_definitions"})
@XmlRootElement
public class TestConfiguration {
    @XmlAttribute(name = "description")
    private String m_description;

    @XmlAttribute(name = "enabled")
    private boolean m_enabled;

    @XmlElement(required = true, name = "TestDefinition")
    private List<TestDefinition> m_definitions;

    public static TestConfiguration fromXml(final String filename) throws JAXBException {
        final File file = new File(filename);
        final JAXBContext jaxbContext = JAXBContext.newInstance(TestConfiguration.class);

        final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (TestConfiguration) jaxbUnmarshaller.unmarshal(file);
    }

    public String getName() {
        return m_description;
    }

    public void setName(final String name) {
        m_description = name;
    }

    public boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(final boolean enabled) {
        m_enabled = enabled;
    }

    public List<TestDefinition> getDefinitions() {
        if (null == m_definitions) {
            m_definitions = new ArrayList<>();
        }

        return m_definitions;
    }

    public void toXml(final String filename) throws JAXBException {
        final File file = new File(filename);
        final JAXBContext jaxbContext = JAXBContext.newInstance(TestConfiguration.class);
        final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        jaxbMarshaller.marshal(this, file);
    }
}
