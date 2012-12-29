package swa.swazam.server.service;

import swa.swazam.server.entity.User;

public interface UserService {
	
	/**
	 * Persists the given user in the database in case he does not already exist
	 * @param u the user which should be newly persisted
	 * @return true, if the user could be successfully stored and false, if the
	 * user already exists
	 */
	public boolean save(User u);
	
	/**
	 * Updates one or more data fields of the given user in case he exists
	 * @param u the user which should be updated
	 * @return true, if the user could be successfully updated and false, if the
	 * user does not exist
	 */
	public boolean update(User u);
	
	/**
	 * Deletes the given user in case he exists
	 * @param username the username of the user which should be deleted
	 * @return true, if the user could be successfully deleted and false, if the
	 * user does not exist	
	 */
	public boolean delete(String username);
	
	/**
	 * Retrieves the given user from the database
	 * @param username the username of the user which should be retrieved
	 * @return the user or null, if no user matching the conditions could be found
	 */
	public User find(String username);
	
	/**
	 * Checks if the given username with the given encrypted password
	 * exist in the database
	 * @param username the name of the user
	 * @param password the encrypted password of the user
	 * @return the user whos login data was given or null 
	 * if the user does not exist or the password is wrong
	 */
	public User login(String username, String password);
}
