package io.vandam.dbdeploy.test.assertion;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Assertion", propOrder = {"m_description", "m_field", "m_assertionComparator", "m_expectedValue"})
public class Assertion {
    @XmlAttribute(name = "description")
    private String m_description;

    @XmlElement(name = "Field")
    private AssertionField m_field;

    @XmlElement(name = "Comparator")
    private AssertionComparator m_assertionComparator;

    @XmlElement(name = "ExpectedValue")
    private String m_expectedValue;

    public String getDescription() {
        return m_description;
    }

    public void setDescription(final String description) {
        m_description = description;
    }

    public AssertionField getField() {
        return m_field;
    }

    public void setField(final AssertionField field) {
        m_field = field;
    }

    public void setComparison(final AssertionComparator assertionComparator) {
        m_assertionComparator = assertionComparator;
    }

    public void setExpectedValue(final String expectedValue) {
        m_expectedValue = expectedValue;
    }

    public boolean compare(final String value) throws UnknownComparatorException {
        final boolean passed;

        switch (m_assertionComparator) {
            case EQUAL:
                passed = (null == m_expectedValue) ? (null == value) : m_expectedValue.equals(value);
                break;

            case NOT_EQUAL:
                passed = (null == m_expectedValue) ? (null != value) : !m_expectedValue.equals(value);
                break;

            default:
                throw new UnknownComparatorException("Unknown m_assertionComparator type " + m_assertionComparator);
        }

        System.out.println("> " + m_field + " [" + (null == value ? "NULL" : value) + "] " + m_assertionComparator + ' ' +
                (null == m_expectedValue ? "NULL" : m_expectedValue) + " : " + (passed ? "PASS" : "FAIL"));

        return passed;
    }

    @Override
    public String toString() {
        return "Assertion{" +
                "m_field='" + m_field + '\'' +
                ", m_assertionComparator=" + m_assertionComparator +
                ", m_expectedValue='" + m_expectedValue + '\'' +
                '}';
    }
}
