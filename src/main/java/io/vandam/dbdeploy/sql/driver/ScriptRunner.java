package io.vandam.dbdeploy.sql.driver;

import java.sql.SQLException;

public class ScriptRunner {

    public static void runScript(final DatabaseDriver databaseDriver, final Iterable<String> sql, final boolean verbose) throws SQLException {
        for (final String aSql : sql) {
            if (null == aSql) {
                throw new NullPointerException("Null query found");
            }

            if (verbose) {
                System.out.println(aSql + ';');
            }

            databaseDriver.query(aSql, null, false);
        }
    }
}
