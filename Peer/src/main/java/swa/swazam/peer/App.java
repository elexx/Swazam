package swa.swazam.peer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

import swa.swazam.peer.alive.AliveManager;
import swa.swazam.peer.preprocessmusic.MusicManager;
import swa.swazam.peer.requestmanagement.RequestManager;
import swa.swazam.util.communication.General2Server;
import swa.swazam.util.communication.Peer2Client;
import swa.swazam.util.communication.Peer2Peer;
import swa.swazam.util.communication.PeerCallback;
import swa.swazam.util.communication.api.PeerCommunicationUtil;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.dto.RequestDTO;
import swa.swazam.util.exceptions.CommunicationException;
import swa.swazam.util.exceptions.SwazamException;
import swa.swazam.util.peerlist.ArrayPeerList;
import swa.swazam.util.peerlist.PeerList;

/**
 * Hello world!
 */
public class App implements Runnable, PeerCallback, PeerController {
	private final AliveManager aliveManager;
	private final RequestManager requestManager;
	private final MusicManager musicManager;

	private PeerCommunicationUtil commLayer;
	private Peer2Client clientStub;
	private General2Server serverStub;
	private Peer2Peer peerStub;

	private final PeerList<InetSocketAddress> peerList;

	private String musicRoot;
	private CredentialsDTO user;

	public static void main(String[] args) {
		App instance = new App();
		instance.run();
		instance.destroy();
	}

	public App() {
		peerList = new ArrayPeerList<InetSocketAddress>();

		aliveManager = new AliveManager();
		requestManager = new RequestManager();
		musicManager = new MusicManager();

		aliveManager.setup(this);
		requestManager.setup(this);
		musicManager.setup(this);
	}

	public void destroy() {
		aliveManager.destroy();
		requestManager.destroy();
		musicManager.destroy();
	}

	@Override
	public void run() {
		try {
			loadConfig();
		} catch (IOException e2) {
			// TODO: nicer output/logging?
			System.err.println("config loading failed");
			e2.printStackTrace();
		}
		initialMusicScan();

		try {
			setupCommLayer();
		} catch (SwazamException e1) {
			// TODO: nicer output/logging?
			System.err.println("comm setup failed");
			e1.printStackTrace();
			return;
		}

		try {
			if (!verifyCredentials()) {
				// TODO: nicer output/logging?
				System.err.println("credentials invalid");
				return;
			}
		} catch (SwazamException swazEx) {
			// TODO: nicer output/logging?
			System.err.println("credentials verification failed");
			swazEx.printStackTrace();
			return;
		} finally {
			user.setPassword("**CLEARED**");
		}

		getStoredPeerList();
		if (peerList.size() == 0 || greetPeerList() == 0) {
			try {
				requestPeerList();
			} catch (CommunicationException e) {
				// TODO: nicer output/logging?
				System.err.println("peer list fetching failed (and not enough stored peers)");
				e.printStackTrace();
				return;
			}

			greetPeerList();
		}
	}

	private void loadConfig() throws IOException {
		Properties configFile = new Properties();
		configFile.load(this.getClass().getClassLoader().getResourceAsStream("peer.properties"));

		String username = configFile.getProperty("credentials.user");
		String password = configFile.getProperty("credentials.pass");

		user = new CredentialsDTO(username, password);

		musicRoot = configFile.getProperty("music.root");
	}

	private void initialMusicScan() {
		musicManager.scan(musicRoot);
	}

	private void setupCommLayer() throws SwazamException {
		commLayer = swa.swazam.util.communication.api.CommunicationUtilFactory.createPeerCommunicationUtil();
		commLayer.setCallback(this);
		commLayer.startup();

		clientStub = commLayer.getClientStub();
		serverStub = commLayer.getServerStub();
		peerStub = commLayer.getPeerStub();
	}

	private boolean verifyCredentials() throws SwazamException {
		return serverStub.verifyCredentials(new CredentialsDTO(user.getUsername(), user.getPassword()));
	}

	private void getStoredPeerList() {
		// TODO
	}

	private void requestPeerList() throws CommunicationException {
		try {
			peerList.addAll(serverStub.getPeerList());
		} catch (CommunicationException cEx) {
			throw cEx;
		} catch (SwazamException swaEx) {
			throw new RuntimeException("unexpected exception while requesting peer list", swaEx);
		}
	}

	private int greetPeerList() {
		List<InetSocketAddress> successful = peerStub.alive(peerList);
		return successful.size();
	}

	@Override
	public MusicManager getMusicManager() {
		return musicManager;
	}

	@Override
	public void alive(InetSocketAddress sender) {
		aliveManager.alive(sender);
	}

	@Override
	public void process(RequestDTO request) {
		requestManager.process(request);
	}

	@Override
	public PeerList<InetSocketAddress> getPeerList() {
		return peerList;
	}

	@Override
	public void solveRequest(RequestDTO request, String artist, String title) {
		clientStub.solved(new MessageDTO(request.getUuid(), title, artist, user), request.getClient());
	}

	@Override
	public void forwardRequest(RequestDTO request) {
		peerStub.process(request, peerList.getTop(5));
	}
}
