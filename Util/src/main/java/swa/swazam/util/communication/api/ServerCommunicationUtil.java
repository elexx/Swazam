package swa.swazam.util.communication.api;

import swa.swazam.util.communication.ServerCallback;

public interface ServerCommunicationUtil extends Startable {

	/**
	 * Methods of callback will be called in case they are called on the opposite side of the network connection
	 * 
	 * @param callback
	 */
	public void setCallback(ServerCallback callback);

}
