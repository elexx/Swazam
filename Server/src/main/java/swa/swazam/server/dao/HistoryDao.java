package swa.swazam.server.dao;

import java.util.List;

import swa.swazam.server.entity.Request;

public interface HistoryDao {
	
	/**
	 * Retrieves all recognition requests that the given user has
	 * initiated.
	 * @param username the username of the user
	 * @return a list containing all found recognition requests or null
	 * if no recognition requests were found.
	 */
	public List<Request> getAllRequestsFromUser(String username);
}
