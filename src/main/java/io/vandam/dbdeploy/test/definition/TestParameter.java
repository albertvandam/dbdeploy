package io.vandam.dbdeploy.test.definition;

import io.vandam.dbdeploy.sql.procedure.ParameterType;
import io.vandam.dbdeploy.sql.procedure.Direction;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Parameter", propOrder = {"m_direction", "m_type", "m_value"})
public class TestParameter {
    @XmlAttribute(name="direction")
    private Direction m_direction;

    @XmlAttribute(name = "type")
    private ParameterType m_type;

    @XmlValue
    private String m_value;

    public Direction getDirection() {
        return m_direction;
    }

    public void setDirection(final Direction direction) {
        m_direction = direction;
    }

    public ParameterType getType() {
        return m_type;
    }

    public void setType(final ParameterType type) {
        m_type = type;
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        m_value = value;
    }
}
