package swa.swazam.util.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MixedCommunicationTest {
	private ServerServerSideCommunication server;
	private ClientServerSideCommunication client;
	private PeerServerSideCommunication peer;

	private Client2Server client2Server;
	private General2Server peer2Server;
	private Peer2Peer peer2Peer;
	private Peer2Client peer2Client;

	@Before
	public void startupClient() throws Exception {
		server = new ServerServerSideCommunication();
		server.startup();

		client = new ClientServerSideCommunication();
		client.startup();
		client2Server = client.getCommUtil().getServerStub();

		peer = new PeerServerSideCommunication();
		peer.startup();
		peer2Server = peer.getCommUtil().getServerStub();
		peer2Peer = peer.getCommUtil().getPeerStub();
		peer2Client = peer.getCommUtil().getClientStub();
	}

	@After
	public void shutdownClient() {
		peer.shutdown();
		client.shutdown();
		server.shutdown();
	}

	@Test(timeout = 5000)
	public void client2Server_getPeerlistTest() throws Exception {
		assertEquals(server.peerListClient, client2Server.getPeerList());
	}

	@Test(timeout = 5000)
	public void client2Server_verifyCredentialsTest() throws Exception {
		assertTrue(client2Server.verifyCredentials(server.credentialsWithCoins));
	}

	@Test(timeout = 5000)
	public void client2Server_hasCoins1Test() throws Exception {
		assertTrue(client2Server.hasCoins(server.credentialsWithCoins));
	}

	@Test(timeout = 5000)
	public void client2Server_hasCoins2Test() throws Exception {
		assertFalse(client2Server.hasCoins(server.credentialsWithoutCoins));
	}

	@Test(timeout = 5000)
	public void peer2Server_getPeerlistTest() throws Exception {
		assertEquals(server.peerListPeer, peer2Server.getPeerList());
	}

	@Test(timeout = 5000)
	public void peer2Server_verifyCredentialsTest() throws Exception {
		assertTrue(peer2Server.verifyCredentials(server.credentialsWithCoins));
	}

	// just testing if process returns.
	@Test(timeout = 5000)
	public void peer2Peer_processToRandomPeersTest() {
		peer2Peer.process(peer.request, Arrays.asList(new InetSocketAddress("localhost", 1234), new InetSocketAddress("localhost", 2345)));
		assertTrue(true);
	}

	@Test(timeout = 5000)
	public void peer2Peer_aliveToRandomPeersTest() {
		assertEquals(0, peer2Peer.alive(Arrays.asList(new InetSocketAddress("localhost", 1234), new InetSocketAddress("localhost", 2345))).size());
	}

	// just testing if solved returns.
	@Test(timeout = 5000)
	public void peer2Client_solved() {
		peer2Client.solved(client.answer, new InetSocketAddress("localhost", 1234));
		assertTrue(true);
	}
}
