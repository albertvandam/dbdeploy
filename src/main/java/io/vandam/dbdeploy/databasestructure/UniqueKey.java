//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, v2.2.11
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2016.04.12 at 07:50:56 PM CAT
//

package io.vandam.dbdeploy.databasestructure;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uniqueKey", propOrder = {"name", "columns"})
public class UniqueKey {

    @XmlElement(required = true)
    private String name;
    @XmlElement(required = true)
    private List<String> columns;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        name = value;
    }

    /**
     * Gets the value of the columns property.
     *
     * @return possible object is
     */
    public List<String> getColumns() {
        if (null == columns) {
            columns = new ArrayList<>();
        }
        return columns;
    }

    /**
     * Equals.
     *
     * @param obj the other
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }

        if (!UniqueKey.class.equals(obj.getClass())) {
            return false;
        }

        final UniqueKey other = (UniqueKey) obj;

        boolean resp = (null == name) ? (null == other.name) : name.equals(other.name);
        resp &= (null == columns) ? (null == other.columns) : columns.equals(other.columns);
        return resp;
    }

    @Override
    public int hashCode() {
        int result = (null != name) ? name.hashCode() : 0;
        result = (31 * result) + ((null != columns) ? columns.hashCode() : 0);
        return result;
    }
}