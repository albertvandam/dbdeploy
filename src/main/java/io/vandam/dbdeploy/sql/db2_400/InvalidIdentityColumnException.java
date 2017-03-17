package io.vandam.dbdeploy.sql.db2_400;

public class InvalidIdentityColumnException extends Exception {
	private static final long serialVersionUID = 3578263813777687610L;

	public InvalidIdentityColumnException() {
        super("Only INTEGER columns can be IDENTITY columns");
    }
}
