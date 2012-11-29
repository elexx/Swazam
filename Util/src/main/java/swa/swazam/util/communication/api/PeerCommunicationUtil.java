package swa.swazam.util.communication.api;

import swa.swazam.util.communication.PeerCallback;

public interface PeerCommunicationUtil extends Startable {

	/**
	 * Methods of callback will be called in case they are called on the opposite side of the network connection
	 * 
	 * @param callback
	 */
	public void setCallback(PeerCallback callback);

}
