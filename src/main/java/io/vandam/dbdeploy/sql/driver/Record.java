package io.vandam.dbdeploy.sql.driver;

import java.util.HashMap;

class Record extends HashMap<String, String> {
	private static final long serialVersionUID = 7103319009174492644L;

	int getAsInt(final String key) {
        final String value = get(key);

        return Integer.parseInt(value);
    }
}
