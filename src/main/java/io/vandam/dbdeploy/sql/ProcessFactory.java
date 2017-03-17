package io.vandam.dbdeploy.sql;

import io.vandam.dbdeploy.sql.db2_400.Db2_400_Process;
import io.vandam.dbdeploy.sql.mysql.MySql_Process;
import io.vandam.dbdeploy.sql.driver.DriverType;

public final class ProcessFactory {
    /**
     * @param driver  Driver type
     * @return Process implementation
     */
    public static IProcess getProcess(final DriverType driver) {
        switch (driver) {
            case DB2_400:
                return new Db2_400_Process();

            case MYSQL:
                return new MySql_Process();
        }

        return null;
    }
}
