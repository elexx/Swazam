package swa.swazam.util.communication.api;

import java.net.InetSocketAddress;

import swa.swazam.util.communication.Client2Server;
import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.communication.General2Peer;
import swa.swazam.util.communication.api.intern.ClientSide;
import swa.swazam.util.communication.api.intern.ServerSide;
import swa.swazam.util.communication.api.intern.stub.Client2ServerStub;
import swa.swazam.util.communication.api.intern.stub.General2PeerStub;
import swa.swazam.util.exceptions.SwazamException;

class ClientCommunicationUtilImpl implements ClientCommunicationUtil {
	private final ServerSide serverSide;
	private final ClientSide clientSide;
	private final Client2ServerStub serverStub;
	private final General2PeerStub peerStub;

	ClientCommunicationUtilImpl() {
		clientSide = new ClientSide();
		serverStub = new Client2ServerStub(clientSide);
		peerStub = new General2PeerStub(clientSide);
		serverSide = new ServerSide(new InetSocketAddress(0));
	}

	@Override
	public void startup() throws SwazamException {
		serverSide.startup();
		clientSide.startup();
		serverStub.startup();
		peerStub.startup();
	}

	@Override
	public void shutdown() {
		peerStub.shutdown();
		serverStub.shutdown();
		clientSide.shutdown();
		serverSide.shutdown();
	}

	@Override
	public void setCallback(ClientCallback callback) {
		serverSide.setCallback(callback);
	}

	@Override
	public Client2Server getServerStub() {
		return serverStub;
	}

	@Override
	public General2Peer getPeerStub() {
		return peerStub;
	}
}
