package io.vandam.dbdeploy.test.assertion;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="Field", propOrder = {"m_source", "m_row", "m_name"})
public class AssertionField {
    @XmlAttribute(name = "source")
    private AssertionFieldSource m_source;

    @XmlAttribute(name = "row")
    private Integer m_row;

    @XmlValue
    private String m_name;

    public AssertionFieldSource getSource() {
        return m_source;
    }

    public void setSource(final AssertionFieldSource source) {
        m_source = source;
    }

    public int getRow() {
        if (null == m_row) {
            m_row = 0;
        }

        return m_row;
    }

    public void setRow(final int row) {
        m_row = row;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }
}
