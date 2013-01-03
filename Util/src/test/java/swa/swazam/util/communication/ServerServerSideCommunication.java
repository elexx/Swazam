package swa.swazam.util.communication;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import swa.swazam.util.communication.api.CommunicationUtilFactory;
import swa.swazam.util.communication.api.ServerCommunicationUtil;
import swa.swazam.util.dto.CredentialsDTO;

public class ServerServerSideCommunication {
	private ServerCommunicationUtil commUtil;

	protected List<InetSocketAddress> peerListClient;
	protected List<InetSocketAddress> peerListPeer;
	protected CredentialsDTO credentialsWithCoins;
	protected CredentialsDTO credentialsWithoutCoins;

	@Before
	public final void startup() throws Exception {
		peerListClient = new LinkedList<>();
		peerListClient.add(new InetSocketAddress("localhost", 1111));
		peerListClient.add(new InetSocketAddress("127.0.0.1", 2222));
		peerListClient = Collections.unmodifiableList(peerListClient);
		peerListPeer = new LinkedList<>();
		peerListPeer.add(new InetSocketAddress("localhost", 9876));
		peerListPeer.add(new InetSocketAddress("127.0.0.1", 6789));
		peerListPeer = Collections.unmodifiableList(peerListPeer);
		credentialsWithCoins = new CredentialsDTO("testuser", "apassword");
		credentialsWithoutCoins = new CredentialsDTO("pooruser", "anotherpassword");

		commUtil = CommunicationUtilFactory.createServerCommunicationUtil();
		commUtil.startup();
		commUtil.setCallback(mockServer());
	}

	@After
	public final void shutdown() {
		commUtil.shutdown();
	}

	private ServerCallback mockServer() throws Exception {
		ServerCallback callback = mock(ServerCallback.class);
		when(callback.getPeerList()).thenReturn(peerListClient);
		when(callback.getPeerList(any(InetSocketAddress.class))).thenReturn(peerListPeer);
		when(callback.verifyCredentials(credentialsWithCoins)).thenReturn(true);
		when(callback.hasCoins(credentialsWithCoins)).thenReturn(true);
		when(callback.hasCoins(credentialsWithoutCoins)).thenReturn(false);
		return callback;
	}

	public ServerCommunicationUtil getCommUtil() {
		return commUtil;
	}
}
