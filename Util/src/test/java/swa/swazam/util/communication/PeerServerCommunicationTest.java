package swa.swazam.util.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import swa.swazam.util.communication.api.CommunicationUtilFactory;
import swa.swazam.util.communication.api.PeerCommunicationUtil;
import swa.swazam.util.exceptions.SwazamException;

public class PeerServerCommunicationTest extends ServerServerSideCommunication {

	private PeerCommunicationUtil commUtil;
	private General2Server serverStub;

	@Before
	public void startupClient() throws SwazamException {
		commUtil = CommunicationUtilFactory.createPeerCommunicationUtil();
		serverStub = commUtil.getServerStub();
		commUtil.startup();
	}

	@After
	public void shutdownClient() {
		commUtil.shutdown();
	}

	@Test(timeout = 5000)
	public void getPeerlistTest() throws Exception {
		assertEquals(peerListPeer, serverStub.getPeerList());
	}

	@Test(timeout = 5000)
	public void verifyCredentialsTest() throws Exception {
		assertTrue(serverStub.verifyCredentials(credentialsWithCoins));
	}
}
