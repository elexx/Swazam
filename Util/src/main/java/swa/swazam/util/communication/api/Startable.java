package swa.swazam.util.communication.api;

import swa.swazam.util.exceptions.CommunicationException;
import swa.swazam.util.exceptions.SwazamException;

interface Startable {

	/**
	 * A generic startup method which will start listening and accepting connections.
	 * 
	 * @throws CommunicationException in case something unexpected happened on the network layer
	 */
	public void startup() throws SwazamException;

	/**
	 * A generic shutdown method which will close all sockets and stop listening for new connections.
	 */
	public void shutdown();

}
