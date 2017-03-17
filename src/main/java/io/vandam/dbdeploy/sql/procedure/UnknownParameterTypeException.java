package io.vandam.dbdeploy.sql.procedure;

public class UnknownParameterTypeException extends Exception {
	private static final long serialVersionUID = 8777399927483709091L;

	UnknownParameterTypeException(final String message) {
        super(message);
    }
}
