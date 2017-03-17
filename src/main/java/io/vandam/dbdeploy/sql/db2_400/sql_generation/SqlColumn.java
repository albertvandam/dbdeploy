package io.vandam.dbdeploy.sql.db2_400.sql_generation;

import io.vandam.dbdeploy.sql.db2_400.InvalidIdentityColumnException;
import io.vandam.dbdeploy.databasestructure.Column;
import io.vandam.dbdeploy.databasestructure.ColumnType;
import io.vandam.dbdeploy.sql.db2_400.InvalidDefaultValueException;

class SqlColumn {
    static String getColumn(final Column col) throws InvalidIdentityColumnException, InvalidDefaultValueException {
        return getColumn(col, false);
    }

    static String getColumn(final Column col, final boolean isCreateTable) throws InvalidIdentityColumnException, InvalidDefaultValueException {
        final StringBuilder sql = new StringBuilder();

        sql.append(col.getType());

        if ((ColumnType.INTEGER == col.getType()) && col.isIdentity()) {
            sql.append(" GENERATED ALWAYS AS IDENTITY (\n");
            sql.append("\t\tSTART WITH 1 INCREMENT BY 1\n");
            sql.append("\t\tNO MINVALUE NO MAXVALUE\n");
            sql.append("\t\tNO CYCLE NO ORDER\n");
            sql.append("\t\tCACHE 20\n");
            sql.append("\t)");

        } else if (col.isIdentity()) {
            throw new InvalidIdentityColumnException();

        } else {
            switch (col.getType()) {
                case CHAR:
                case VARCHAR:
                    sql.append('(').append(col.getSize("#0")).append(')');
                    break;

                case DECIMAL:
                    sql.append('(').append(col.getSize("#0.0").replace('.', ',')).append(')');
                    if (null == col.getDefaultValue()) {
                        col.setDefaultValue("");
                    }

                    if ((null != col.getDefaultValue()) && !col.getDefaultValue().isEmpty()) {
                        col.setDefaultValue(Double.parseDouble(col.getDefaultValue()));
                    }
                    break;

                case INTEGER:
                case BIGINT:
                    if ((null != col.getDefaultValue()) && !col.getDefaultValue().isEmpty()) {
                        col.setDefaultValue(Integer.parseInt(col.getDefaultValue()));
                    }
                    break;

                case DATE:
                    if ((null != col.getDefaultValue()) && !col.getDefaultValue().isEmpty()) {
                        if (!"current_date".equals(col.getDefaultValue().toLowerCase())) {
                            throw new InvalidDefaultValueException(col.getDefaultValue() + " not a valid default value");
                        }
                    }
                    break;

                case TIME:
                    if ((null != col.getDefaultValue()) && !col.getDefaultValue().isEmpty()) {
                        if (!"current_time".equals(col.getDefaultValue().toLowerCase())) {
                            throw new InvalidDefaultValueException(col.getDefaultValue() + " not a valid default value");
                        }
                    }
                    break;

                case TIMESTAMP:
                    if ((null != col.getDefaultValue()) && !col.getDefaultValue().isEmpty()) {
                        if (!"current_timestamp".equals(col.getDefaultValue().toLowerCase())) {
                            throw new InvalidDefaultValueException(col.getDefaultValue() + " not a valid default value");
                        }
                    }
                    break;

                default:
                    break;
            }

            sql.append(col.canBeNull() ? "" : " NOT NULL");

            sql.append(((null == col.getDefaultValue()) || col.getDefaultValue().isEmpty()) ? "" : (" DEFAULT " + col.getDefaultValue()));

            if (!isCreateTable) {
                sql.append(col.canBeNull() ? " DROP NOT NULL" : "");
            }
        }

        return sql.toString();
    }
}
