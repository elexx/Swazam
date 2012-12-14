package swa.swazam.util.exceptions;

public class CommunicationException extends SwazamException {

	private static final long serialVersionUID = -8816384199804168137L;

	public CommunicationException() {}

	public CommunicationException(String message) {
		super(message);
	}

	public CommunicationException(Throwable cause) {
		super(cause);
	}

	public CommunicationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommunicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
