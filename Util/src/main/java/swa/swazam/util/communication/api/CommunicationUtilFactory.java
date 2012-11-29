package swa.swazam.util.communication.api;

public class CommunicationUtilFactory {

	/**
	 * Creates a new {@link ServerCommunicationUtil}.
	 * 
	 * @return
	 */
	public static ServerCommunicationUtil createServerCommunicationUtil() {
		return new ServerCommunicationUtilImpl();
	}

	/**
	 * Creates a new {@link ClientCommunicationUtil}.
	 * 
	 * @return
	 */
	public static ClientCommunicationUtil createClientCommunicationUtil() {
		return new ClientCommunicationUtilImpl();
	}

	/**
	 * Creates a new {@link PeerCommunicationUtil}.
	 * 
	 * @return
	 */
	public static PeerCommunicationUtil createPeerCommunicationUtil() {
		return new PeerCommunicationUtilImpl();
	}

}
