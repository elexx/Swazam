package swa.swazam.util.communication;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import swa.swazam.util.communication.api.ClientCommunicationUtil;
import swa.swazam.util.communication.api.CommunicationUtilFactory;
import swa.swazam.util.communication.api.ServerCommunicationUtil;

public class ShutdownTest {

	private ServerCommunicationUtil commUtilServer;
	private ClientCommunicationUtil commUtilClient;
	private List<InetSocketAddress> peerList;

	@Before
	public void startup() throws Exception {
		commUtilServer = CommunicationUtilFactory.createServerCommunicationUtil(new InetSocketAddress(9090));
		commUtilServer.setCallback(mockServer());
		commUtilServer.startup();

		commUtilClient = CommunicationUtilFactory.createClientCommunicationUtil(new InetSocketAddress(9090));
		commUtilClient.startup();

		peerList = new LinkedList<>();
		peerList.add(new InetSocketAddress("localhost", 1234));
		peerList.add(new InetSocketAddress("127.0.0.1", 4567));
		peerList = Collections.unmodifiableList(peerList);
	}

	@Test(timeout = 5000)
	public void clientThenServerShutdown() throws Exception {
		commUtilClient.getServerStub().getPeerList();
		commUtilClient.shutdown();
		commUtilServer.shutdown();
		assertTrue(true);
	}

	@Test(timeout = 5000)
	public void serverThenClientShutdown() throws Exception {
		commUtilClient.getServerStub().getPeerList();
		commUtilServer.shutdown();
		commUtilClient.shutdown();
		assertTrue(true);
	}

	private ServerCallback mockServer() throws Exception {
		ServerCallback callback = mock(ServerCallback.class);
		when(callback.getPeerList()).thenReturn(peerList);
		return callback;
	}
}
