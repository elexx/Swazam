package swa.swazam.util.communication;

import java.net.InetSocketAddress;
import java.util.List;

import swa.swazam.util.dto.RequestDTO;

public interface General2Peer {

	/**
	 * Forwards the given request to the peers given in the list.
	 * @param request the request that should be forwarded
	 * @param destinationPeers a list containing the contact information of the peers, 
	 * to which the request should be forwarded.
	 */
	public void process(RequestDTO request, List<InetSocketAddress> destinationPeers);

}
