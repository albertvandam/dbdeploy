package io.vandam.dbdeploy.procedure;

public class Parameter {
    private String m_mode;
    private String m_name;
    private String m_type;
    private String m_length;
    private String m_default;

    public void setMode(final String mode) {
        m_mode = mode;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public void setType(final String type) {
        m_type = type;
    }

    public void setLength(final String length) {
        m_length = length;
    }

    public void setDefault(final String aDefault) {
        m_default = aDefault;
    }

    String getMode() {
        return m_mode;
    }

    public String getName() {
        return m_name;
    }

    public String getType() {
        return m_type;
    }

    public String getLength() {
        return m_length;
    }

    public String getDefault() {
        return m_default;
    }
}
