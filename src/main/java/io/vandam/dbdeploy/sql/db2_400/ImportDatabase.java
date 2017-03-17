package io.vandam.dbdeploy.sql.db2_400;

import io.vandam.dbdeploy.databasestructure.*;
import io.vandam.dbdeploy.configuration.DatabaseConfig;
import io.vandam.dbdeploy.sql.ImportDatabaseAbstract;
import io.vandam.dbdeploy.sql.driver.DatabaseDriver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class ImportDatabase.
 */
class ImportDatabase extends ImportDatabaseAbstract {
    /**
     * Instantiates a new import database.
     *
     * @param databaseConfig the database config
     */
    ImportDatabase(final DatabaseConfig databaseConfig) {
        super(databaseConfig);
    }

    /**
     * Gets the tables.
     *
     * @param db the db
     * @return the tables
     */
    @Override
    protected boolean getTables(final Database db) {

        final List<String> parameters = new ArrayList<>();
        parameters.add(m_databaseConfig.getSchemaName());

        String sql = "SELECT SYSTEM_TABLE_NAME, TABLE_NAME, TABLE_TEXT ";
        sql += "FROM QSYS2.SYSTABLES ";
        sql += "WHERE TABLE_SCHEMA=? AND TABLE_NAME NOT LIKE 'Q%' ";
        sql += "ORDER BY SYSTEM_TABLE_NAME";

        boolean resp = true;
        try {
            final ResultSet res = m_databaseDriver.query(sql, parameters, true).getResultSet();
            if (null != res) {
                while (res.next()) {
                    System.out.println("Found table " + res.getString("TABLE_NAME"));

                    db.putTable(new Table(res.getString("TABLE_NAME"), res.getString("SYSTEM_TABLE_NAME"),
                            res.getString("TABLE_TEXT")));
                }

                res.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            resp = false;
        }

        return resp;
    }

    /**
     * Gets the columns.
     *
     * @param db the db
     * @return the columns
     */
    @Override
    protected boolean getColumns(final Database db) {
        boolean resp = true;

        int i = 0;
        while (resp && (i < db.getTables().size())) {
            final List<String> parameters = new ArrayList<>();
            parameters.add(m_databaseConfig.getSchemaName());
            parameters.add(db.getTables().get(i).getName());

            final StringBuilder sql = new StringBuilder();
            sql.append("SELECT COLUMN_NAME, SYSTEM_COLUMN_NAME, COLUMN_TEXT, DATA_TYPE, LENGTH, NUMERIC_SCALE, ");
            sql.append("IS_NULLABLE, COLUMN_DEFAULT, IS_IDENTITY ");
            sql.append("FROM QSYS2.SYSCOLUMNS sc ");
            sql.append("WHERE sc.TABLE_SCHEMA=? and sc.TABLE_NAME=? ");
            sql.append("ORDER BY sc.TABLE_NAME, sc.ORDINAL_POSITION");

            try {
                final ResultSet res = m_databaseDriver.query(sql.toString(), parameters, true).getResultSet();
                if (null != res) {
                    while (res.next()) {
                        final String name = res.getString("COLUMN_NAME");
                        final String systemName = res.getString("SYSTEM_COLUMN_NAME");
                        String description = res.getString("COLUMN_TEXT");
                        description = (null == description) ? "" : description;

                        String dataType = res.getString("DATA_TYPE").trim().toUpperCase();
                        if ("TIMESTMP".equals(dataType)) {
                            dataType = "TIMESTAMP";
                        }
                        final ColumnType type = ColumnType.fromValue(dataType);

                        String scale = res.getString("NUMERIC_SCALE");
                        scale = (null == scale) ? "0" : scale;

                        final double size = Double.parseDouble(res.getString("LENGTH") + '.' + scale);
                        final boolean canBeNull = "Y".equals(res.getString("IS_NULLABLE").substring(0, 1));
                        final String defaultValue = res.getString("COLUMN_DEFAULT");
                        final boolean identity = "Y".equals(res.getString("IS_IDENTITY").substring(0, 1));

                        System.out.println(
                                "On " + db.getTables().get(i).getName() + " found column " + name + ' ' + type + '('
                                        + size + ") " + (canBeNull ? "NULL" : "NOT NULL") + (identity ? " IDENTITY" : "")
                                        + (null == defaultValue ? "" : " DEFAULT '" + defaultValue + '\'')
                                        + (description.isEmpty() ? "" : " : " + description));

                        db.getTables().get(i).putColumn(new Column(name, systemName, description, type, size, canBeNull,
                                defaultValue, identity));
                    }

                    res.close();
                }
            } catch (final Exception e) {
                e.printStackTrace();
                resp = false;
            }

            i++;
        }

        return resp;
    }

    /**
     * Gets the primary keys.
     *
     * @param database the db
     * @return the primary keys
     */
    @Override
    protected boolean getPrimaryKeys(final Database database) {
        final String sql = "SELECT DISTINCT c.COLUMN_NAME, c.ORDINAL_POSITION " +
                "FROM QSYS2.SYSKEYCST c " +
                "LEFT JOIN QSYS2.SYSCST k ON " +
                "(c.CONSTRAINT_NAME=k.CONSTRAINT_NAME AND c.TABLE_NAME=k.TABLE_NAME) " +
                "WHERE c.TABLE_SCHEMA=? AND c.TABLE_NAME=? AND k.CONSTRAINT_TYPE='PRIMARY KEY' " +
                "ORDER BY c.ORDINAL_POSITION";

        return populatePrimaryKeys(database, sql);
    }

    /**
     * Gets the check constraints.
     *
     * @param db the db
     * @return the check constraints
     */
    @Override
    protected boolean getCheckConstraints(final Database db) {
        boolean resp = true;

        int i = 0;
        while (resp && (i < db.getTables().size())) {
            final List<String> parameters = new ArrayList<>();
            parameters.add(m_databaseConfig.getSchemaName());
            parameters.add(db.getTables().get(i).getName());

            final StringBuilder sql = new StringBuilder();
            sql.append("SELECT cc.CONSTRAINT_NAME, cc.CHECK_CLAUSE ");
            sql.append("FROM QSYS2.SYSCST c ");
            sql.append("LEFT JOIN QSYS2.CHECK_CONSTRAINTS cc ON ");
            sql.append("(c.CONSTRAINT_SCHEMA=cc.CONSTRAINT_SCHEMA AND c.CONSTRAINT_NAME=cc.CONSTRAINT_NAME) ");
            sql.append("WHERE c.TABLE_SCHEMA=? AND c.TABLE_NAME=? AND c.CONSTRAINT_TYPE='CHECK'");

            try {
                final ResultSet res = m_databaseDriver.query(sql.toString(), parameters, true).getResultSet();
                if (null != res) {
                    while (res.next()) {
                        System.out.println("On " + db.getTables().get(i).getName() + " found check constraint "
                                + res.getString("CONSTRAINT_NAME") + " : " + res.getString("CHECK_CLAUSE"));

                        db.getTables().get(i).putCheckConstraint(
                                new CheckConstraint(res.getString("CONSTRAINT_NAME"), res.getString("CHECK_CLAUSE")));
                    }

                    res.close();
                }
            } catch (final Exception e) {
                e.printStackTrace();
                resp = false;
            }

            i++;
        }

        return resp;
    }

    /**
     * Gets the foreign keys.
     *
     * @param db the db
     * @return the foreign keys
     */
    @Override
    protected boolean getForeignKeys(final Database db) {
        boolean resp = true;

        int i = 0;
        while (resp && (i < db.getTables().size())) {
            final List<String> parameters = new ArrayList<>();
            parameters.add(m_databaseConfig.getSchemaName());
            parameters.add(db.getTables().get(i).getName());

            final StringBuilder sql = new StringBuilder();
            sql.append("SELECT DISTINCT ");
            sql.append("  child.CONSTRAINT_NAME As constraint_name, ");
            sql.append("  parent.TABLE_NAME As parent_table_name, ");
            sql.append("  parent.COLUMN_NAME As parent_column_name, ");
            sql.append("  child.COLUMN_NAME As child_column_name ");
            sql.append("FROM ");
            sql.append("  QSYS2.SYSKEYCST child ");
            sql.append("    INNER JOIN QSYS2.SYSREFCST crossref ON ");
            sql.append("      child.CONSTRAINT_SCHEMA = crossref.CONSTRAINT_SCHEMA AND ");
            sql.append("      child.CONSTRAINT_NAME = crossref.CONSTRAINT_NAME ");
            sql.append("    INNER JOIN QSYS2.SYSKEYCST parent ON ");
            sql.append("      crossref.UNIQUE_CONSTRAINT_SCHEMA = parent.CONSTRAINT_SCHEMA AND ");
            sql.append("      crossref.UNIQUE_CONSTRAINT_NAME = parent.CONSTRAINT_NAME ");
            sql.append("    INNER JOIN QSYS2.SYSCST coninfo ON ");
            sql.append("      child.CONSTRAINT_NAME = coninfo.CONSTRAINT_NAME ");
            sql.append("WHERE child.TABLE_SCHEMA=? AND child.TABLE_NAME=? AND coninfo.CONSTRAINT_TYPE='FOREIGN KEY'");
            sql.append("ORDER BY child.CONSTRAINT_NAME");

            try {
                final ResultSet res = m_databaseDriver.query(sql.toString(), parameters, true).getResultSet();
                if (null != res) {
                    String pConstraint = "";
                    ForeignKey foreignKey = null;

                    while (res.next()) {
                        final String constraintName = res.getString("CONSTRAINT_NAME");
                        if (!pConstraint.equals(constraintName)) {
                            if (!pConstraint.isEmpty()) {
                                db.getTables().get(i).putForeignKey(foreignKey);
                            }

                            pConstraint = constraintName;

                            foreignKey = new ForeignKey();
                            foreignKey.setName(constraintName);
                            foreignKey.setReferenceTable(res.getString("PARENT_TABLE_NAME"));

                            System.out.println("On " + db.getTables().get(i).getName() + " found foreign key "
                                    + constraintName + " referencing " + res.getString("PARENT_TABLE_NAME"));
                        }

                        System.out.println("On " + db.getTables().get(i).getName() + " found foreign key column "
                                + res.getString("CHILD_COLUMN_NAME") + " referencing "
                                + res.getString("PARENT_COLUMN_NAME") + " on " + pConstraint);

                        final String child_column_name = res.getString("CHILD_COLUMN_NAME");
                        if ((null != foreignKey) && !foreignKey.getColumns().contains(child_column_name)) {
                            foreignKey.getColumns().add(child_column_name);
                        }

                        final String parent_column_name = res.getString("PARENT_COLUMN_NAME");
                        if ((null != foreignKey) && !foreignKey.getReferenceColumns().contains(parent_column_name)) {
                            foreignKey.getReferenceColumns().add(parent_column_name);
                        }
                    }

                    if (!pConstraint.isEmpty()) {
                        db.getTables().get(i).putForeignKey(foreignKey);
                    }

                    res.close();
                }
            } catch (final Exception e) {
                e.printStackTrace();
                resp = false;
            }

            i++;
        }

        return resp;
    }

    /**
     * Gets the unique keys.
     *
     * @param db the db
     * @return the unique keys
     */
    @Override
    protected boolean getUniqueKeys(final Database db) {
        final String sql = "SELECT DISTINCT c.CONSTRAINT_NAME, c.COLUMN_NAME, c.ORDINAL_POSITION " +
                "FROM QSYS2.SYSKEYCST c " +
                "LEFT JOIN QSYS2.SYSCST k ON " +
                "(c.CONSTRAINT_NAME=k.CONSTRAINT_NAME AND c.TABLE_NAME=k.TABLE_NAME) " +
                "WHERE c.TABLE_SCHEMA=? AND c.TABLE_NAME=? AND k.CONSTRAINT_TYPE='UNIQUE' " +
                "ORDER BY c.CONSTRAINT_NAME, c.ORDINAL_POSITION";

        return populateUniqueKeys(db, sql);
    }

    /**
     * Gets the indexes.
     *
     * @param db the db
     * @return the indexes
     */
    @Override
    protected boolean getIndexes(final Database db) {
        boolean resp = true;

        int i = 0;
        while (resp && (i < db.getTables().size())) {
            final List<String> parameters = new ArrayList<>();
            parameters.add(m_databaseConfig.getSchemaName());
            parameters.add(db.getTables().get(i).getName());

            final StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append("INDEX_NAME, SYSTEM_INDEX_NAME, INDEX_TEXT, COLUMN_NAMES ");
            sql.append("FROM QSYS2.SYSINDEXSTAT ");
            sql.append("WHERE TABLE_SCHEMA=? AND TABLE_NAME=? ");
            sql.append("ORDER BY INDEX_NAME");

            try {
                final ResultSet res = m_databaseDriver.query(sql.toString(), parameters, true).getResultSet();
                if (null != res) {
                    while (res.next()) {
                        final Index index = new Index();
                        index.setName(res.getString("INDEX_NAME"));
                        index.setSystemName(res.getString("SYSTEM_INDEX_NAME"));
                        index.setDescription(res.getString("INDEX_TEXT"));
                        index.setColumns(Arrays.asList(res.getString("COLUMN_NAMES").replace(" ", "").split(",")));

                        db.getTables().get(i).getIndexes().add(index);
                        System.out.println("On " + db.getTables().get(i).getName() + " found index "
                                + res.getString("INDEX_NAME"));
                    }

                    res.close();
                }
            } catch (final Exception e) {
                e.printStackTrace();
                resp = false;
            }

            i++;
        }

        return resp;
    }

    /**
     * Gets the database.
     *
     * @return the database
     * @throws ClassNotFoundException the class not found exception
     */
    @Override
    public Database getDatabase() throws ClassNotFoundException {
        final Database response = new Database();

        m_databaseDriver = new DatabaseDriver(m_databaseConfig, Connection.TRANSACTION_NONE);

        m_databaseDriver.connect();

        if (!getTables(response)) {
            m_databaseDriver.close();
            return null;
        }

        if (!getColumns(response)) {
            m_databaseDriver.close();
            return null;
        }

        if (!getPrimaryKeys(response)) {
            m_databaseDriver.close();
            return null;
        }

        if (!getCheckConstraints(response)) {
            m_databaseDriver.close();
            return null;
        }

        if (!getForeignKeys(response)) {
            m_databaseDriver.close();
            return null;
        }

        if (!getUniqueKeys(response)) {
            m_databaseDriver.close();
            return null;
        }

        if (!getIndexes(response)) {
            m_databaseDriver.close();
            return null;
        }

        m_databaseDriver.close();

        return response;
    }
}
