package io.vandam.dbdeploy.test.definition;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Statement", propOrder = {"m_type", "m_statement", "m_parameters"})
public class TestStatement {
    @XmlAttribute(name="type")
    private TestType m_type;

    @XmlElement(name="Statement")
    private String m_statement;

    @XmlElement(name="Parameter")
    private List<TestParameter> m_parameters;

    public TestType getType() {
        return m_type;
    }

    public void setType(final TestType type) {
        m_type = type;
    }

    public String getStatement() {
        return m_statement;
    }

    public void setStatement(final String statement) {
        m_statement = statement;
    }

    public List<TestParameter> getParameters() {
        if (null == m_parameters) {
            m_parameters = new ArrayList<>();
        }

        return m_parameters;
    }
}
