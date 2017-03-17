package io.vandam.dbdeploy.sql.procedure;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.sql.Types;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"m_direction", "m_parameterType"})
public class Parameter {
    @XmlElement(name = "Direction")
    private Direction m_direction;

    @XmlElement(name = "ParameterType")
    private ParameterType m_parameterType;

    /**
     * Constructor - left for JAXB
     */
    public Parameter() {
        // Left for JAXB
    }

    /**
     * Constructor
     *
     * @param direction     Parameter direction
     * @param parameterType Parameter type
     */
    public Parameter(final Direction direction, final ParameterType parameterType) {
        m_direction = direction;
        m_parameterType = parameterType;
    }

    public Direction getDirection() {
        return m_direction;
    }

//    public void setDirection(final Direction direction) {
//        m_direction = direction;
//    }

//    public ParameterType getParameterType() {
//        return m_parameterType;
//    }

//    public void setParameterType(final ParameterType parameterType) {
//        m_parameterType = parameterType;
//    }

    public int getParameterSqlType() throws UnknownParameterTypeException {
        switch (m_parameterType) {
            case CHAR:
                return Types.CHAR;

            case VARCHAR:
                return Types.VARCHAR;

            case INTEGER:
                return Types.INTEGER;

            case DECIMAL:
                return Types.DECIMAL;

            default:
                throw new UnknownParameterTypeException("Unknown parameter type " + m_parameterType);
        }
    }
}
