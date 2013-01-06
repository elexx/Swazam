package swa.swazam.util.communication.api;

import java.net.InetSocketAddress;

public class CommunicationUtilFactory {

	/**
	 * Creates a new {@link ServerCommunicationUtil}.
	 * 
	 * @param listenAddress Do not bind, connect or close this InetSocketAddress
	 * @return
	 */
	public static ServerCommunicationUtil createServerCommunicationUtil(InetSocketAddress listenAddress) {
		return new ServerCommunicationUtilImpl(listenAddress);
	}

	/**
	 * Creates a new {@link ClientCommunicationUtil}.
	 * 
	 * @param listenAddress Do not bind, connect or close this InetSocketAddress
	 * @return
	 */
	public static ClientCommunicationUtil createClientCommunicationUtil(InetSocketAddress serverAddress) {
		return new ClientCommunicationUtilImpl(serverAddress);
	}

	/**
	 * Creates a new {@link PeerCommunicationUtil}.
	 * 
	 * @param listenAddress Do not bind, connect or close this InetSocketAddress
	 * @return
	 */
	public static PeerCommunicationUtil createPeerCommunicationUtil(InetSocketAddress serverAddress) {
		return new PeerCommunicationUtilImpl(serverAddress);
	}
}
