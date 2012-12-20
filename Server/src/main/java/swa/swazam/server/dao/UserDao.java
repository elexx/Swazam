package swa.swazam.server.dao;

import swa.swazam.server.entity.User;

public interface UserDao {
	
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
}
