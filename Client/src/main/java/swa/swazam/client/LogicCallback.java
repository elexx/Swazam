package swa.swazam.client;

import java.io.File;
import java.util.UUID;

import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.exceptions.SwazamException;

public interface LogicCallback {

	/**
	 * Tries to login a user. If this fails an already logged in user will get
	 * logged out.
	 * 
	 * @param credentials
	 * @return
	 * @throws SwazamException
	 */
	boolean login(CredentialsDTO credentials) throws SwazamException;

	/**
	 * Checks if a user is currently logged in.
	 * 
	 * @return
	 */
	boolean isLoggedIn();

	/**
	 * returns true if the currently logged in user has coins.
	 * 
	 * @return
	 * @throws SwazamException
	 *             if a network problem occurs or if
	 *             {@link LogicCallback#isLoggedIn()} would return false.
	 */
	boolean hasCoins() throws SwazamException;

	/**
	 * Processes the chosen file and returns the UUID of this request.
	 * 
	 * @param selectedFile
	 * @return
	 * @throws SwazamException
	 *             if a network problem occurs or if
	 *             {@link LogicCallback#isLoggedIn()} would return false.
	 */
	UUID fileChosen(File selectedFile) throws SwazamException;

	/**
	 * Shuts down the client logic, performing any resource-releasing necessary
	 */
	void shutdown();

}
