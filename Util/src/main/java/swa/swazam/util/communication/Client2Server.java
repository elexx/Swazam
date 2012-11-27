package swa.swazam.util.communication;

import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.SwazamException;

public interface Client2Server extends General2Server{
	
	/**
	 * Requests, if the given user has any coins left
	 * @param user the user to check for
	 * @throws SwazamException in case the credentials are wrong or communication fails
	 * @return true, if the user has at least 1 coin left, else false
	 */
	public boolean hasCoins(CredentialsDTO user) throws SwazamException;
	
	/**
	 * Sends the log information for a request to the server (either to tell it that a new request was started or to
	 * update an existing request)
	 * In case of a new request, a coin will be paid from the user's account.
	 * In case of an update of an existing request, the resolver earns a coin.
	 * @throws SwazamException in case the credentials are wrong or communication fails
	 */
	public void logRequest(CredentialsDTO user, MessageDTO message) throws SwazamException;
}
