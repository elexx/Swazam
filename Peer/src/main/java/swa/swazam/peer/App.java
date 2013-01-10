package swa.swazam.peer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

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
import swa.swazam.util.peerlist.PeerListBackup;

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
	private String storageRoot;
	private CredentialsDTO user;

	private String serverHostname;
	private int serverPort;

	private final String configPath;

	public static void main(String[] args) {
		ParameterSet params = readCommandLineParams(args);
		if (params == null) return;

		App instance = new App(params);
		instance.run();
		instance.inputLoop();
		instance.destroy();
	}

	private static ParameterSet readCommandLineParams(String[] args) {
		ParameterSet params = new ParameterSet();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			if (arg.equals("-c") || arg.equals("--config")) {
				if (++i < args.length) {
					params.configFile = args[i];
				} else {
					System.err.println("Missing config file path");
					return null;
				}
			} else {
				System.err.println("Unknown parameter " + args[i]);
				return null;
			}
		}
		return params;
	}

	public App(ParameterSet parameters) {
		peerList = new ArrayPeerList<InetSocketAddress>();

		aliveManager = new AliveManager();
		requestManager = new RequestManager();
		musicManager = new MusicManager();

		aliveManager.setup(this);
		requestManager.setup(this);
		musicManager.setup(this);

		this.configPath = parameters.configFile;
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

		try {
			getStoredPeerList();
		} catch (SwazamException swazEx) {}

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

	private void inputLoop() {
		Scanner input = new Scanner(System.in);

		while (input.hasNextLine()) {
			String cmd = input.next();

			if (cmd.equals("quit") || cmd.equals("exit") || cmd.equals("bye")) break;
			else if (cmd.equals("add")) {
				System.out.println("\"add\" command not yet implemented");
			} else {
				System.err.println("Unknown command: " + cmd);
			}
		}
	}

	private void loadConfig() throws IOException {
		Properties configFile = new Properties();
		if (configPath != null) {
			configFile.load(this.getClass().getClassLoader().getResourceAsStream(configPath));
		} else {
			configFile.load(this.getClass().getClassLoader().getResourceAsStream("peer.properties"));
		}

		String username = configFile.getProperty("credentials.user");
		String password = configFile.getProperty("credentials.pass");

		user = new CredentialsDTO(username, password);

		musicRoot = configFile.getProperty("music.root");
		storageRoot = configFile.getProperty("storage.root");

		serverHostname = configFile.getProperty("server.hostname", "localhost");
		serverPort = Integer.valueOf(configFile.getProperty("server.port", "9090"));
	}

	private void initialMusicScan() {
		musicManager.scan(musicRoot);
	}

	private void setupCommLayer() throws SwazamException {
		commLayer = swa.swazam.util.communication.api.CommunicationUtilFactory.createPeerCommunicationUtil(new InetSocketAddress(serverHostname, serverPort));
		commLayer.setCallback(this);
		commLayer.startup();

		clientStub = commLayer.getClientStub();
		serverStub = commLayer.getServerStub();
		peerStub = commLayer.getPeerStub();
	}

	private boolean verifyCredentials() throws SwazamException {
		return serverStub.verifyCredentials(new CredentialsDTO(user.getUsername(), user.getPassword()));
	}

	private void getStoredPeerList() throws SwazamException {
		PeerListBackup plb = new PeerListBackup(storageRoot);
		peerList.addAll(plb.loadPeers());
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
