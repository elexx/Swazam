package swa.swazam.util.communication.api;

import java.net.InetSocketAddress;

import swa.swazam.util.communication.General2Server;
import swa.swazam.util.communication.Peer2Client;
import swa.swazam.util.communication.PeerCallback;
import swa.swazam.util.communication.api.intern.ClientSide;
import swa.swazam.util.communication.api.intern.ServerSide;
import swa.swazam.util.communication.api.intern.stub.Peer2ClientStub;
import swa.swazam.util.communication.api.intern.stub.Peer2PeerStub;
import swa.swazam.util.communication.api.intern.stub.Peer2ServerStub;
import swa.swazam.util.exceptions.SwazamException;

class PeerCommunicationUtilImpl implements PeerCommunicationUtil {

	private final ServerSide serverSide;
	private final ClientSide clientSide;
	private final Peer2ServerStub serverStub;
	private final Peer2ClientStub clientStub;
	private final Peer2PeerStub peerStub;

	PeerCommunicationUtilImpl(InetSocketAddress serverAddress) {
		clientSide = new ClientSide();
		serverStub = new Peer2ServerStub(clientSide, serverAddress);
		clientStub = new Peer2ClientStub(clientSide);
		peerStub = new Peer2PeerStub(clientSide);
		serverSide = new ServerSide(new InetSocketAddress(0));
	}

	@Override
	public void startup() throws SwazamException {
		serverSide.startup();
		serverStub.startup();
		clientStub.startup();
		peerStub.startup();

		InetSocketAddress localListenAddress = new InetSocketAddress(serverStub.reportSendingAddress(), serverSide.getEffectivePort());
		serverStub.updateLocalListenAddress(localListenAddress);
		clientStub.updateLocalListenAddress(localListenAddress);
		peerStub.updateLocalListenAddress(localListenAddress);
	}

	@Override
	public void shutdown() {
		serverStub.shutdown();
		clientStub.shutdown();
		peerStub.shutdown();
		clientSide.shutdown();
		serverSide.shutdown();
	}

	@Override
	public void setCallback(PeerCallback callback) {
		serverSide.setCallback(callback);
	}

	@Override
	public General2Server getServerStub() {
		return serverStub;
	}

	@Override
	public Peer2Client getClientStub() {
		return clientStub;
	}

	@Override
	public Peer2Peer getPeerStub() {
		return peerStub;
	}
}
