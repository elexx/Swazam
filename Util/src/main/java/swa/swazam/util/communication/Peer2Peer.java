package swa.swazam.util.communication;

import java.net.InetSocketAddress;
import java.util.List;

public interface Peer2Peer extends General2Peer {
	
	/**
	 * Sends an alive message to all peers given in the list and determines, whether they
	 * are alive. 
	 * Whenever a peer comes online, it sends a ping to at most 100 other known peers.
	 * In case less than 5 of these peers answer, the requesting peer has to contact
	 * the server and request a new peer list. 
	 * @param destinationPeers a list containing at most 100 other peers that should be contacted
	 * @return a list containing all peers, that have answered the request or an empty list, 
	 * in case no peers sent an answer. 
	 */
	public List<InetSocketAddress> alive(List<InetSocketAddress> destinationPeers);
	
}
