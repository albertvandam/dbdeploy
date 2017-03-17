package io.vandam.dbdeploy.test.definition;

public class UnknownTestTypeException extends Exception {
	private static final long serialVersionUID = -967321463688342635L;

	public UnknownTestTypeException(final String message) {
        super(message);
    }
}
