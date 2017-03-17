package io.vandam.dbdeploy.sql.procedure;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = "m_parameters")
public class Procedure {
    @XmlElement(name = "Parameters")
    private List<Parameter> m_parameters;

    public List<Parameter> getParameters() {
        return m_parameters;
    }

    public void setParameters(final List<Parameter> parameters) {
        m_parameters = parameters;
    }
}
