package io.vandam.dbdeploy.test.definition;

import io.vandam.dbdeploy.test.assertion.Assertion;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestDefinition", propOrder = {"m_id", "m_description", "m_enabled", "m_beforeTest", "m_test", "m_assertions"})
public class TestDefinition {
    @XmlAttribute(name = "identifier")
    private String m_id;

    @XmlAttribute(name = "description")
    private String m_description;

    @XmlAttribute(name = "enabled")
    private boolean m_enabled;

    @XmlElement(name = "BeforeTest")
    private List<TestStatement> m_beforeTest;

    @XmlElement(name = "Test")
    private TestStatement m_test;

    @XmlElement(name = "Assertion")
    private List<Assertion> m_assertions;

    public String getId() {
        return m_id;
    }

    public void setId(final String value) {
        m_id = value.replaceAll("[^A-Za-z0-9]", "");
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(final String description) {
        m_description = description;
    }

    public boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(final boolean enabled) {
        m_enabled = enabled;
    }

    public TestStatement getTest() {
        if (null == m_test) {
            m_test = new TestStatement();
        }

        return m_test;
    }

    public void setTest(final TestStatement test) {
        m_test = test;
    }

    public List<Assertion> getAssertions() {
        if (null == m_assertions) {
            m_assertions = new ArrayList<>();
        }

        return m_assertions;
    }

    public Collection<TestStatement> getBeforeTest() {
        if (null == m_beforeTest) {
            m_beforeTest = new ArrayList<>();
        }

        return m_beforeTest;
    }
}
