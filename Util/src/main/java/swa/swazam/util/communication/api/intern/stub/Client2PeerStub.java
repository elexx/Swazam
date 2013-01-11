package swa.swazam.util.communication.api.intern.stub;

import java.net.InetSocketAddress;
import java.util.List;

import swa.swazam.util.communication.General2Peer;
import swa.swazam.util.communication.api.Startable;
import swa.swazam.util.communication.api.intern.ClientSide;
import swa.swazam.util.dto.RequestDTO;
import swa.swazam.util.exceptions.SwazamException;

public class Client2PeerStub extends Peer2PeerStub implements General2Peer, Startable {

	private InetSocketAddress localListenAddress;

	public Client2PeerStub(ClientSide clientSide) {
		super(clientSide);
	}

	@Override
	public void startup() throws SwazamException {}

	@Override
	public void shutdown() {}

	@Override
	public void process(RequestDTO request, List<InetSocketAddress> destinationPeers) {
		request.setClient(localListenAddress);
		super.process(request, destinationPeers);
	}

	public void updateLocalListenAddress(InetSocketAddress localListenAddress) {
		this.localListenAddress = localListenAddress;
	}
}
