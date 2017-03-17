package io.vandam.dbdeploy.procedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Procedure {
    private String m_type;
    private List<Parameter> m_parameters;
    private String m_name;
    private String m_deterministic;
//    private String m_sqlAccess;
    private int m_resultSetQty;
//    private String m_commitOnReturn;
    private String m_description;
    private String m_body;
    private String m_systemName;

    public void setType(final String routine) {
        m_type = routine;
    }

    public Collection<Parameter> getParameters() {
        if (null == m_parameters) {
            m_parameters = new ArrayList<>();
        }

        return m_parameters;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public void setDeterministic(final String deterministic) {
        m_deterministic = deterministic;
    }

//    public void setSqlAccess(final String sqlAccess) {
//        m_sqlAccess = sqlAccess;
//    }

    public void setResultSetQty(final String resultSetQty) {
        m_resultSetQty = Integer.parseInt(resultSetQty);
    }

//    public void setCommitOnReturn(final String commitOnReturn) {
//        m_commitOnReturn = commitOnReturn;
//    }

    public void setDescription(final String description) {
        m_description = description;
    }

    public void setBody(final String body) {
        m_body = body;
    }

    public String getSql() {
        final StringBuilder sql = new StringBuilder();
        sql.append("CREATE OR REPLACE PROCEDURE ").append(m_name).append(" (\n");

        boolean first = true;
        for (final Parameter parameter : m_parameters) {
            if (first) {
                first = false;
            } else {
                sql.append(",\n");
            }
            sql.append('\t').append(parameter.getMode()).append(' ').append(parameter.getName()).append(' ').append(parameter.getType());

            if (!"INTEGER".equals(parameter.getType().trim().toUpperCase())) {
                sql.append('(').append(parameter.getLength()).append(')');
            }

            if ((null != parameter.getDefault()) && !parameter.getDefault().isEmpty()) {
                sql.append(" DEFAULT ").append(parameter.getDefault());
            }
        }

        sql.append("\n)\n");

        if (0 != m_resultSetQty) {
            sql.append("\tDYNAMIC RESULT SETS 1\n");
        }

        sql.append("\tLANGUAGE ").append(m_type).append('\n');
        sql.append("\tSPECIFIC ").append(m_systemName).append('\n');
        if ("no".equals(m_deterministic.toLowerCase().trim())) {
            sql.append("\tNOT DETERMINISTIC\n");
        } else {
            sql.append("\tDETERMINISTIC\n");
        }
        sql.append("\tMODIFIES SQL DATA\n");
        sql.append("\tCALLED ON NULL INPUT\n");
        sql.append("\tPROGRAM TYPE SUB\n");
        sql.append("\tSET OPTION ALWBLK=*ALLREAD,\n");
        sql.append("\t\tALWCPYDTA=*OPTIMIZE,\n");
        sql.append("\t\tCOMMIT=*NONE,\n");
        sql.append("\t\tDECRESULT=(31, 31, 00),\n");
        sql.append("\t\tDFTRDBCOL=*NONE,\n");
        sql.append("\t\tDYNUSRPRF=*USER,\n");
        sql.append("\t\tDYNDFTCOL=*NO,\n");
        sql.append("\t\tSQLPATH=*LIBL,\n");
        sql.append("\t\tSRTSEQ=*HEX,\n");
        sql.append("\t\tDLYPRP =*NO\n");

        sql.append(m_body).append('\n');

        if (null != m_description) {
            sql.append("LABEL ON PROCEDURE ").append(m_name).append(" TEXT IS '").append(m_description).append("'\n");
        }

        return sql.toString();
    }

    public String getName() {
        return m_name;
    }

    public String getSystemName() {
        return m_systemName;
    }

    public void setSystemName(final String systemName) {
        m_systemName = systemName;
    }
}
