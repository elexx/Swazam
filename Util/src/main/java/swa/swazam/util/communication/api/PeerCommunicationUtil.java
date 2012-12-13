package swa.swazam.util.communication.api;

import swa.swazam.util.communication.General2Server;
import swa.swazam.util.communication.Peer2Client;
import swa.swazam.util.communication.Peer2Peer;
import swa.swazam.util.communication.PeerCallback;

public interface PeerCommunicationUtil extends Startable {

	/**
	 * Methods of callback will be called in case they are called on the
	 * opposite side of the network connection
	 * 
	 * @param callback
	 */
	public void setCallback(PeerCallback callback);

	/**
	 * returns an RMI-like server stub object
	 * 
	 * @return
	 */
	public General2Server getServerStub();

	/**
	 * returns an RMI-like client stub object
	 * 
	 * @return
	 */
	public Peer2Client getClientStub();

	/**
	 * returns an RMI-like peer stub object
	 * 
	 * @return
	 */
	public Peer2Peer getPeerStub();

}
