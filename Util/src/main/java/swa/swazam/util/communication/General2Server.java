package swa.swazam.util.communication;

import java.net.InetSocketAddress;
import java.util.List;

import swa.swazam.util.exceptions.SwazamException;

/**
 * General communication interface for peer and client.
 */
public interface General2Server {
	
	/**
	 * Delivers a list containing the top 5 peers.
	 * If less are known, less are delivered.
	 * @throws SwazamException in case the communication fails
	 * @return the top 5 peers (at most)
	 */
	public List<InetSocketAddress> getPeerList() throws SwazamException;
}
