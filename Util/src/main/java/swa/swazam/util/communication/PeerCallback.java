package swa.swazam.util.communication;

import java.net.InetSocketAddress;

import swa.swazam.util.dto.RequestDTO;

public interface PeerCallback {

	/**
	 * Handles an alive message
	 * @param sender the peer sending the request
	 */
	public void alive(InetSocketAddress sender);
	
	/**
	 * Handles the given request
	 * @param request the request that should be processed
	 */
	public void process(RequestDTO request);
}
