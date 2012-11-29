package swa.swazam.util.communication.api;

interface Startable {

	/**
	 * A generic startup method which will start listening and accepting connections.
	 */
	public void startup();

	/**
	 * A generic shutdown method which will close all sockets and stop listening for new connections.
	 */
	public void shutdown();

}
