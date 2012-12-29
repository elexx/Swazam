package swa.swazam.server.service;

import java.util.List;

import swa.swazam.server.entity.Request;

public interface HistoryService {
	
	/**
	 * Delivers all recognition requests that the given user has
	 * requested.
	 * @param username the username of the user
	 * @return a list containing all found recognition requests or an empty list
	 * if no recognition requests were found.
	 */
	public List<Request> getAllRequestedRequestsFromUser(String username);
	
	/**
	 * Delivers all recognition requests that the given user has
	 * solved.
	 * @param username the username of the user
	 * @return a list containing all found recognition requests or an empty list
	 * if no recognition requests were found.
	 */
	public List<Request> getAllSolvedRequestsFromUser(String username);

}
