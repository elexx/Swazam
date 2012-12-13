package swa.swazam.util.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import swa.swazam.util.communication.api.ClientCommunicationUtil;
import swa.swazam.util.communication.api.CommunicationUtilFactory;
import swa.swazam.util.exceptions.SwazamException;

public class ClientServerCommunicationTest extends AbstractServerCommunication {

	private ClientCommunicationUtil clientCommUtil;
	private Client2Server serverStub;

	@Before
	public void startupClient() throws SwazamException {
		clientCommUtil = CommunicationUtilFactory.createClientCommunicationUtil();
		clientCommUtil.startup();
		serverStub = clientCommUtil.getServerStub();
	}

	@After
	public void shutdownClient() {
		clientCommUtil.shutdown();
	}

	@Test
	public void getPeerlistTest() throws Exception {
		assertEquals(peerList, serverStub.getPeerList());
	}

	@Test
	public void verifyCredentialsTest() throws Exception {
		assertTrue(serverStub.verifyCredentials(credentialsWithCoins));
	}

	@Test
	public void hasCoins1Test() throws Exception {
		assertTrue(serverStub.hasCoins(credentialsWithCoins));
	}

	@Test
	public void hasCoins2Test() throws Exception {
		assertFalse(serverStub.hasCoins(credentialsWithoutCoins));
	}
}
