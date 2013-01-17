package swa.swazam.server.dao;

import java.util.List;

import swa.swazam.server.entity.Request;

public interface HistoryDao {
	
	/**
	 * Retrieves all recognition requests that the given user has
	 * requested.
	 * @param username the username of the user
	 * @return a list containing all found recognition requests or an empty list
	 * if no recognition requests were found.
	 */
	public List<Request> getAllRequestedRequestsFromUser(String username);
	
	/**
	 * Retrieves all recognition requests that the given user has
	 * solved.
	 * @param username the username of the user
	 * @return a list containing all found recognition requests or an empty list
	 * if no recognition requests were found.
	 */
	public List<Request> getAllSolvedRequestsFromUser(String username);

	/**
	 * Saves the given request in the database
	 * @param request the request that should be stored
	 */
	public boolean save(Request request);
	
	/**
	 * Updates the given request in the database
	 * @param request the request that should be updated
	 */
	public boolean update(Request request);
	
	/**
	 * Looks for the given request
	 * @param requestUUID the UUID as string of the request that should be found
	 * @return the request if existing or null, if no request with the given UUID exists
	 */
	public Request find(String requestUUID);
}
