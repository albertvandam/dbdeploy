package io.vandam.dbdeploy.sql.mysql;

import io.vandam.dbdeploy.databasestructure.*;
import io.vandam.dbdeploy.sql.driver.DatabaseDriver;
import io.vandam.dbdeploy.configuration.DatabaseConfig;
import io.vandam.dbdeploy.sql.ImportDatabaseAbstract;

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

        String sql = "SELECT TABLE_NAME, TABLE_COMMENT ";
        sql += "FROM information_schema.TABLES ";
        sql += "WHERE TABLE_SCHEMA=?";

        boolean resp = true;
        try {
            final ResultSet res = m_databaseDriver.query(sql, parameters, true).getResultSet();
            if (null != res) {
                while (res.next()) {
                    System.out.println("Found table " + res.getString("TABLE_NAME"));
                    
                    final String name = res.getString("TABLE_NAME");
                    String systemName = "";
                    String description = res.getString("TABLE_COMMENT");
                    
                    if (description.contains("|")) {
                    	String[] detail = description.split("|");
                    	systemName = detail[0];
                    	description = detail[1];
                    }

                    db.putTable(new Table(name, systemName, description));
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
            sql.append("SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, COALESCE(CHARACTER_MAXIMUM_LENGTH, ");
            sql.append("NUMERIC_PRECISION) AS LENGTH, NUMERIC_SCALE, IS_NULLABLE, COLUMN_DEFAULT, EXTRA, COLUMN_TYPE ");
            sql.append("FROM information_schema.COLUMNS ");
            sql.append("WHERE TABLE_SCHEMA=? AND TABLE_NAME=? ");
            sql.append("ORDER BY TABLE_NAME, ORDINAL_POSITION");

            try {
                final ResultSet res = m_databaseDriver.query(sql.toString(), parameters, true).getResultSet();
                if (null != res) {
                    while (res.next()) {
                        final String name = res.getString("COLUMN_NAME").trim();
                        String systemName = "";
                        String description = res.getString("COLUMN_COMMENT").trim();
                        
                        if (description.contains("|")) {
                        	String[] detail = description.split("|");
                        	systemName = detail[0];
                        	description = detail[1];
                        }

                        String length = res.getString("LENGTH");
                        length = (null == length) ? "0" : length.trim();

                        String dataType = res.getString("DATA_TYPE").trim().toUpperCase();
                        switch (dataType) {
                            case "TEXT":
                                dataType = "VARCHAR";
                                length = "32740";
                                break;

                            case "DATETIME":
                                dataType = "TIMESTAMP";
                                break;

                            case "INT":
                            case "TINYINT":
                            case "SET":
                                dataType = "INTEGER";
                                break;

                            case "BIGINT":
                                dataType = "BIGINT";
                                break;

                            case "FLOAT":
                                dataType = "DECIMAL";
                                break;

                            case "ENUM":
                                dataType = "CHAR";

                                String opt = res.getString("COLUMN_TYPE");
                                if (opt.contains("(")) {
                                    opt = opt.substring(opt.indexOf('('));
                                }

                                System.out.println("Adding check constraint " + name + "_ENUM : " + name + " IN " + opt);

                                db.getTables().get(i)
                                        .putCheckConstraint(new CheckConstraint(name + "_ENUM", name + " IN " + opt));

                                opt = opt.substring(1, opt.length() - 1).replace("'", "");
                                final String[] options = opt.split(",");
                                int ml = 0;
                                for (final String option : options) {
                                    ml = Integer.max(ml, option.trim().length());
                                }

                                length = Integer.toString(ml);
                                break;
                        }
                        final ColumnType type = ColumnType.fromValue(dataType);

                        String scale = res.getString("NUMERIC_SCALE");
                        scale = (null == scale) ? "0" : scale.trim();

                        final double size = Double.parseDouble(length + '.' + scale);
                        final boolean canBeNull = "Y".equals(res.getString("IS_NULLABLE").substring(0, 1));
                        String defaultValue = res.getString("COLUMN_DEFAULT");
                        defaultValue = (null == defaultValue) ? null : defaultValue.trim();
                        final boolean identity = res.getString("EXTRA").contains("auto_increment");

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
        final String sql = "SELECT column_name aS COLUMN_NAME, seq_in_index AS ORDINAL_POSITION " +
                "FROM information_schema.statistics " +
                "WHERE table_schema = ? AND table_name=? AND index_name='PRIMARY' " +
                "ORDER BY seq_in_index";

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
        return true;
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
            sql.append("SELECT k.CONSTRAINT_NAME, k.COLUMN_NAME, k.REFERENCED_TABLE_NAME, ");
            sql.append("REFERENCED_COLUMN_NAME ");
            sql.append("FROM information_schema.REFERENTIAL_CONSTRAINTS c ");
            sql.append("LEFT JOIN information_schema.KEY_COLUMN_USAGE k ON (c.CONSTRAINT_SCHEMA=k.CONSTRAINT_SCHEMA AND ");
            sql.append("c.TABLE_NAME=k.TABLE_NAME AND c.CONSTRAINT_NAME=k.CONSTRAINT_NAME) ");
            sql.append("WHERE c.CONSTRAINT_SCHEMA=? AND c.TABLE_NAME=? ");
            sql.append("ORDER BY k.CONSTRAINT_NAME, k.ORDINAL_POSITION");

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
                            foreignKey.setReferenceTable(res.getString("REFERENCED_TABLE_NAME"));
                            System.out.println("On " + db.getTables().get(i).getName() + " found foreign key "
                                    + constraintName + " referencing " + res.getString("REFERENCED_TABLE_NAME"));
                        }

                        System.out.println("On " + db.getTables().get(i).getName() + " found foreign key column "
                                + res.getString("COLUMN_NAME") + " referencing "
                                + res.getString("REFERENCED_COLUMN_NAME") + " on " + pConstraint);

                        final String column_name = res.getString("COLUMN_NAME");
                        if ((null != foreignKey) && !foreignKey.getColumns().contains(column_name)) {
                            foreignKey.getColumns().add(column_name);
                        }

                        final String referenced_column_name = res.getString("REFERENCED_COLUMN_NAME");
                        if ((null != foreignKey) && !foreignKey.getReferenceColumns().contains(referenced_column_name)) {
                            foreignKey.getReferenceColumns().add(referenced_column_name);
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
        final String sql = "SELECT index_name AS CONSTRAINT_NAME, column_name AS COLUMN_NAME, seq_in_index AS ORDINAL_POSITION " +
                "FROM information_schema.statistics " +
                "WHERE table_schema = ? AND table_name=? AND INDEX_NAME != 'PRIMARY' AND non_unique='0' " +
                "ORDER BY index_name, seq_in_index";

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
            sql.append("SELECT INDEX_NAME, COLUMN_NAME, INDEX_COMMENT ");
            sql.append("FROM information_schema.statistics ");
            sql.append("WHERE TABLE_SCHEMA=? AND TABLE_NAME=? AND INDEX_NAME != 'PRIMARY' AND NON_UNIQUE != '0' ");
            sql.append("ORDER BY INDEX_NAME, SEQ_IN_INDEX");

            try {
                final ResultSet res = m_databaseDriver.query(sql.toString(), parameters, true).getResultSet();
                if (null != res) {
                    String pConstraint = "";
                    Index index = null;

                    while (res.next()) {
                        final String constraintName = res.getString("INDEX_NAME");
                        if (!pConstraint.equals(constraintName)) {
                            if (!pConstraint.isEmpty()) {
                                db.getTables().get(i).getIndexes().add(index);
                            }

                            pConstraint = constraintName;
                            index = new Index();
                            index.setName(constraintName);
                            index.setSystemName("");
                            index.setDescription(res.getString("INDEX_COMMENT"));

                            System.out.println(
                                    "On " + db.getTables().get(i).getName() + " found index " + constraintName);
                        }

                        System.out.println("On " + db.getTables().get(i).getName() + " found index column "
                                + res.getString("COLUMN_NAME") + " on " + pConstraint);

                        if (null != index) {
                            index.setColumns(Arrays.asList(res.getString("COLUMN_NAME").split(",")));
                        }
                    }

                    if (!pConstraint.isEmpty()) {
                        db.getTables().get(i).getIndexes().add(index);
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

        m_databaseDriver = new DatabaseDriver(m_databaseConfig, -1);

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
