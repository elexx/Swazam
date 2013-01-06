package swa.swazam.util.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import swa.swazam.util.communication.api.ClientCommunicationUtil;
import swa.swazam.util.communication.api.CommunicationUtilFactory;
import swa.swazam.util.exceptions.SwazamException;

public class ClientServerCommunicationTest extends ServerServerSideCommunication {

	private ClientCommunicationUtil commUtil;
	private Client2Server serverStub;

	@Before
	public void startupClient() throws SwazamException {
		commUtil = CommunicationUtilFactory.createClientCommunicationUtil(new InetSocketAddress(9090));
		commUtil.startup();
		serverStub = commUtil.getServerStub();
	}

	@After
	public void shutdownClient() {
		commUtil.shutdown();
	}

	@Test(timeout = 5000)
	public void getPeerlistTest() throws Exception {
		assertEquals(peerListClient, serverStub.getPeerList());
	}

	@Test(timeout = 5000)
	public void verifyCredentialsTest() throws Exception {
		assertTrue(serverStub.verifyCredentials(credentialsWithCoins));
	}

	@Test(timeout = 5000)
	public void hasCoins1Test() throws Exception {
		assertTrue(serverStub.hasCoins(credentialsWithCoins));
	}

	@Test(timeout = 5000)
	public void hasCoins2Test() throws Exception {
		assertFalse(serverStub.hasCoins(credentialsWithoutCoins));
	}
}
