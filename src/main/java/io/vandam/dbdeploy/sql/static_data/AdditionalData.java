package io.vandam.dbdeploy.sql.static_data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "AdditionalData")
@XmlAccessorType(XmlAccessType.FIELD)
public class AdditionalData {
	@XmlAttribute(name="field")
	private String m_field;
	
	@XmlValue
	private String m_value;

	public AdditionalData() {
	}

	public AdditionalData(final String field, final String value) {
		m_field = field;
		m_value = value;
	}

	public String getField() {
		return m_field;
	}

	public void setField(String field) {
		m_field = field;
	}

	public String getValue() {
		return m_value;
	}

	public void setValue(String value) {
		m_value = value;
	}
}
