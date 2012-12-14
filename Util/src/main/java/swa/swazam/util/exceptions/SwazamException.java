package swa.swazam.util.exceptions;

public class SwazamException extends Exception {

	private static final long serialVersionUID = -1275962031852157917L;

	public SwazamException() {}

	public SwazamException(String message) {
		super(message);
	}

	public SwazamException(Throwable cause) {
		super(cause);
	}

	public SwazamException(String message, Throwable cause) {
		super(message, cause);
	}

	public SwazamException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
