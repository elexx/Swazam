/**
 * 
 */
package swa.swazam.clientnew;

import swa.swazam.util.exceptions.SwazamException;

/**
 * @author michael
 * 
 */
public class InsufficientParametersException extends SwazamException {

	private static final long serialVersionUID = -2730647067192348967L;

	public InsufficientParametersException() {}

	public InsufficientParametersException(String message) {
		super(message);
	}

	public InsufficientParametersException(Throwable cause) {
		super(cause);
	}

	public InsufficientParametersException(String message, Throwable cause) {
		super(message, cause);
	}

}
