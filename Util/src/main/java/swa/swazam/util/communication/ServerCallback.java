package swa.swazam.util.communication;

import java.net.InetSocketAddress;
import java.util.List;

import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.SwazamException;

public interface ServerCallback {

	/**
	 * Delivers a list containing the top 5 peers. If less are known, less are delivered.<br />
	 * This method will be called if a client requests the list.
	 * 
	 * @throws SwazamException in case the communication fails
	 * @return the top 5 peers (at most)
	 */
	public List<InetSocketAddress> getPeerList() throws SwazamException;

	/**
	 * Delivers a list containing the top 5 peers. If less are known, less are delivered.<br />
	 * This method will be called if a peer requests the list.
	 * 
	 * @param sender the sender of this request
	 * @throws SwazamException in case the communication fails
	 * @return the top 5 peers (at most)
	 */
	public List<InetSocketAddress> getPeerList(InetSocketAddress sender) throws SwazamException;

	/**
	 * Requests, if the given user has any coins left
	 * 
	 * @param user the user to check for
	 * @throws SwazamException in case the credentials are wrong or communication fails
	 * @return true, if the user has at least 1 coin left, else false
	 */
	public boolean hasCoins(CredentialsDTO user) throws SwazamException;

	/**
	 * Sends the log information for a request to the server (either to tell it that a new request was started or to update an existing request) In case of a new request, a coin will be paid from the user's account. In case of an update of an existing request, the resolver earns a coin.
	 * 
	 * @throws SwazamException in case the credentials are wrong or communication fails
	 */
	public void logRequest(CredentialsDTO user, MessageDTO message) throws SwazamException;

	/**
	 * Checks if the given user is registered at the server
	 * 
	 * @param user
	 * @return true, if the given user exists and the username and passwort is correct, else false.
	 * @throws SwazamException in case the communication fails
	 */
	public boolean verifyCredentials(CredentialsDTO user) throws SwazamException;

}
