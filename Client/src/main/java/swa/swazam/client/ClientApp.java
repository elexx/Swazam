package swa.swazam.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.UUID;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import swa.swazam.client.gui.AppGUI;
import swa.swazam.util.communication.Client2Server;
import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.communication.General2Peer;
import swa.swazam.util.communication.api.ClientCommunicationUtil;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.dto.RequestDTO;
import swa.swazam.util.exceptions.SwazamException;
import swa.swazam.util.fingerprint.FingerprintTools;
import swa.swazam.util.hash.HashGenerator;
import swa.swazam.util.peerlist.ArrayPeerList;
import swa.swazam.util.peerlist.PeerList;
import swa.swazam.util.peerlist.PeerListBackup;
import ac.at.tuwien.infosys.swa.audio.Fingerprint;

/**
 * App represents the client application of swazam. The app manages a peer list, can take a music recording, fingerprint it, and send it to peers to identify it.
 */
public class ClientApp implements ProgressHandler {

	private static final String TESTDATA = "demo";
	private static final String TESTFILE = TESTDATA + ".wav";
	private static final int MAGICPEERNUMBER = 5; // has to be at least 2
	static private final String newline = "\n";

	private String snippetRootDirectory;

	// private final RequestManager requestManager;

	private ClientCommunicationUtil commLayer;
	private Client2Server serverStub;
	private General2Peer peerStub;

	private CredentialsDTO user;
	private RequestDTO request;
	private MessageDTO message;

	private PeerList<InetSocketAddress> peerList;

	private ClientCallback clientCallback;
	private InetSocketAddress clientSocketAddress;
	private int clientPort;
	private InetSocketAddress serverAddress;
	private BufferedReader br;
	private PeerListBackup peerListBackup;
	private String storagePath;
	private String snippetFileName;
	private int clientID;
	private AppGUI gui;
	private boolean tryAgain;

	private TimeLimiter limiter;

	public ClientApp(int clientID, boolean startGUI) {
		peerList = new ArrayPeerList<>();
		clientCallback = new ClientCallbackImpl(this);
		br = new BufferedReader(new InputStreamReader(System.in));
		this.clientID = clientID;
		if (startGUI) {
			gui = new AppGUI(this);
		}

		// requestManager = new RequestManager();
		// requestManager.setup(this);
	}

	/**
	 * starts SWAzam client app App.jar 2 will look up 2client.properties file
	 * 
	 * @param args can expect an integer with the id of a specific client property file (for simple testing)
	 */
	public static void main(String[] args) {

		int clientID = 0;
		boolean startGUI = false;
		if (args.length > 0) {
			try {
				clientID = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Argument 1 must be an integer which refers to a configuration file eg. 1client.properties  and second parameter can be 'g' to start a gui");
				System.exit(1);
			}
			if ("g".equalsIgnoreCase(args[1])) { // gui
				startGUI = true;
			}
			if ("c".equalsIgnoreCase(args[1])) { // commandline
				startGUI = false;
			}
		}

		ClientApp app = new ClientApp(clientID, startGUI);
		welcomeMessage();
		app.run();
	}

	/**
	 * prints a welcome message for the user
	 */
	private static void welcomeMessage() {
		System.out.println("Welcome to SWAzam!\n");
		System.out.println("To try out our service, we registered already a demo user with a number of coins");
		System.out.println("Login and password are: " + TESTDATA + "\n");
		System.out.println("A test recording, containing a 9 second random song snippet we provide as well: " + TESTFILE);
		System.out.println("Have fun with SWAzam!\n\n");
	}

	public void run() {
		startup();
		try {
			performLogin(); // let user enter username and password on commandline if config file contains testdata
			if (!checkforCoins()) {
				logMessage("No more coins available. You receive coins when your peer program identifies a song request of other clients. Client quitting.");
				System.exit(0);
			}
		} catch (SwazamException e) {
			System.err.println("Server, internet connection, or database are down. Please try again later.");
			System.exit(0);
		}
		if (gui != null) {
			gui.show();
		} else {
			try {
				do {
					logMessage("\nInformation: you can get more coins by running a SWAzam Peer and solving music requests.\n");
					
					boolean searchAgain = searchForSnippet(getSnippetFileToFingerprintFromUser());
					System.out.println("tryAgain: " + searchAgain);
				} while (getRepeatDecissionFromUser());

			} catch (SwazamException e) {
				System.err.println("Server, internet connection, or database are down. Please try again later.");
			} finally {
				shutdown();
			}
		}
	}

	/**
	 * loads configuration from config file, sets up storage for peerListbackup, sets up communication
	 */
	private void startup() {
		try {
			loadConfig();
		} catch (IOException e1) {
			System.err.println("Loading config was not possible.");
			System.exit(0);
		}
		try {
			setupStorage();
		} catch (SwazamException e1) {
			System.err.println("Local peer list not found. Attempting Server.");
		}
		try {
			setupCommLayer();
		} catch (SwazamException e1) {
			System.err.println("Communication setup failed.");
			shutdown();
		}
	}

	/**
	 * reads configuration (server address, client port for peer callback, snippet root directory)
	 * 
	 * @throws IOException
	 */
	protected void loadConfig() throws IOException {
		Properties configFile = new Properties();
		String propertiesFileName = "client.properties";
		if (clientID != 0) {
			propertiesFileName = clientID + "client.properties";
		}
		logMessage("Using properties file: " + propertiesFileName);
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(propertiesFileName);
		configFile.load(is);

		String username = configFile.getProperty("credentials.user").trim();
		String password = configFile.getProperty("credentials.pass").trim();
		logMessage("user: " + username + " and password: " + password + " found in config.");
		user = new CredentialsDTO(username, HashGenerator.hash(password));

		String serverHostname = configFile.getProperty("server.hostname").trim();
		int serverPort = Integer.parseInt(configFile.getProperty("server.port"));
		serverAddress = new InetSocketAddress(Inet4Address.getByName(serverHostname), serverPort);

		clientPort = Integer.parseInt(configFile.getProperty("client.port"));
		clientSocketAddress = new InetSocketAddress(Inet4Address.getLocalHost().getHostAddress(), clientPort);

		snippetRootDirectory = configFile.getProperty("snippet.root").trim();
		snippetFileName = configFile.getProperty("snippet.demofile").trim();
		storagePath = configFile.getProperty("peerlist.storagepath").trim();
	}

	protected void setupCommLayer() throws SwazamException {
		commLayer = swa.swazam.util.communication.api.CommunicationUtilFactory.createClientCommunicationUtil(serverAddress); // clientPort
		commLayer.setCallback(clientCallback);
		commLayer.startup();

		peerStub = commLayer.getPeerStub();
		serverStub = commLayer.getServerStub();
	}

	protected void setupStorage() throws SwazamException {
		storagePath = System.getProperty("user.dir") + storagePath;
		peerListBackup = new PeerListBackup(storagePath);
		peerList.addAll(peerListBackup.loadPeers());
	}

	/**
	 * Login at server with username and password
	 * 
	 * @param username
	 * @param password (not hashed yet)
	 * @return true when user is registered at server and username plus hashed password combination is correct false otherwise
	 * @throws SwazamException if server not reachable for example
	 */
	protected boolean login(String username, String password) throws SwazamException {
		user = new CredentialsDTO(username, HashGenerator.hash(password));

		return serverStub.verifyCredentials(user);
	}

	/**
	 * checks users coin status on server
	 * 
	 * @return true if user has at least one coin left, false otherwise
	 * @throws SwazamException if server not reachable for example
	 */
	private boolean checkforCoins() throws SwazamException {
		return serverStub.hasCoins(user);
	}

	/**
	 * creates the requestDTO for sending to peers and also prepares the messageDTO for the server
	 * 
	 * @param fingerprint
	 */
	private void createRequest(Fingerprint fingerprint) {
		request = new RequestDTO(UUID.randomUUID(), clientSocketAddress, fingerprint);
		message = new MessageDTO(request.getUuid(), "", "", null);
	}

	/**
	 * initially getPeerList from Server or if #Peers in PeerList < MAGICPEERNUMBER (eg. 5)
	 * 
	 * @throws SwazamException
	 */
	private void checkAndUpdateInitialPeerListToMinumumSize() throws SwazamException {
		if (peerList.size() < MAGICPEERNUMBER) {
			PeerList<InetSocketAddress> oldPeers = new ArrayPeerList<>();
			oldPeers.addAll(peerList);
			peerList.clear(); // to avoid duplicates
			peerList.addAll(serverStub.getPeerList()); // add new peers
			for (InetSocketAddress inetSocketAddress : oldPeers) {
				if (!peerList.contains(inetSocketAddress)) {
					peerList.add(0, inetSocketAddress); // add old, presumably good peers, to top of list
				}
			}
		}
		logMessage("Number of peers in list: " + peerList.size());
		logMessage("Using top" + MAGICPEERNUMBER + ": " + peerList.getTop(MAGICPEERNUMBER).toString());
	}

	/**
	 * check if user has coins to do a search, generate a fingerprint, send to peers from peerlist, and displays result
	 * 
	 * @throws SwazamException
	 */
	public boolean searchForSnippet(Fingerprint fingerprint) throws SwazamException {
		createRequest(fingerprint); // create UUID for RequestDTO, create MessageDTO with UUID filled out already
		serverStub.logRequest(user, message); // logRequest sends UUID/MessageDTO to Server

		checkAndUpdateInitialPeerListToMinumumSize();
		
		// send MessageDTO (with UUID fingerprint) to top MAGICPEERNUMBER (eg.5) peers from client peerlist in parallel
		// eventually also receive a return value of a successful or failed connection attempt to first peers, so that more peers in list can be tried without the request failing after first MAGICPEERNUMBER (eg 5) not online
		peerStub.process(request, peerList.getTop(MAGICPEERNUMBER));

		// wait 30 seconds, if no answer, display to user, "song snippet not found, try again later?/discard"
		logMessage("searching");

		limiter = new TimeLimiter();
		if (gui != null) {
			limiter.registerHandler(gui.getProgressHandler());
		} else {
			limiter.registerHandler(this);
		}
		new Thread(limiter).start();
		return tryAgain;
	}

	/**
	 * interactive Command line workflow reading user command line input if same sound snippet should be reused again
	 * 
	 * @return true if same snippet should be used again
	 * @throws SwazamException
	 */
	private boolean getTrySnippetAgainDecisionFromUser() throws SwazamException {
		String answer;
		if (checkforCoins()) {
			logMessage("Try same search again (a) and spend one more coin? or Discard (d) and move on? [a|(d)]:"); // search again with different top MAGICPEERNUMBER (eg 5) or discard and loop back to hascoins check
			try {
				answer = br.readLine();
				if (answer.equalsIgnoreCase("a")) {
					return true;
				} else if (answer.equalsIgnoreCase("d")) {
					return false;
				}
			} catch (IOException e) {
				System.err.println("Could not read input, opting for default answer: Discard");
			}
		}
		return false;
	}

	/**
	 * interactive Command line workflow reading user command line input if client should quit or another snippet should be searched
	 * 
	 * @return true if another snippet should be searched
	 */
	private boolean getRepeatDecissionFromUser() {
		String answer;
		logMessage("Yes (y), spend one more coin for a new search or Quit (q)? [y|(q)]:");
		try {
			answer = br.readLine();
			if ((answer.equalsIgnoreCase("q")) || (answer.equalsIgnoreCase("quit")) || (answer.equalsIgnoreCase("")) || (answer.equalsIgnoreCase("by")) || (answer.equalsIgnoreCase("exit"))) {
				return false;
			} else if (answer.equalsIgnoreCase("d")) {
				return true;
			}
		} catch (IOException e) {
			System.err.println("Could not read input, opting for default answer: Quit");
		}
		return false;
	}

	/**
	 * removes worst performing peers at bottom of list.
	 * 
	 * @param peers
	 * @throws SwazamException
	 */
	private void removePeersFromBottom(int peers) throws SwazamException {
		int size = peerList.size();

		if (size >= (MAGICPEERNUMBER + peers)) { // remove bottom peers when enough in peer list
			for (int i = size - 1; i >= size - 1 - peers; i--) {
				peerList.remove(i);
			}
		} else if (size >= MAGICPEERNUMBER + 1) { // do not remove element with index MAGICPEERNUMBER-1 since it might be new, but all left (# < peers) above
			for (int i = MAGICPEERNUMBER; i <= size - 1; i++) {
				peerList.remove(i);
			}
		} else if (size == MAGICPEERNUMBER) { // peer on MAGICPEERNUMBER-1 index position is possibly new, so remove the one before in the list only containing just enough
			peerList.remove(MAGICPEERNUMBER - 2);
		} else { // fetch new peers from server
			checkAndUpdateInitialPeerListToMinumumSize();
		}
	}

	/**
	 * best replace peers from top with ones below
	 * 
	 * @param peers
	 * @throws SwazamException
	 */
	private void addPeersToTop(int peers) throws SwazamException {
		int size = peerList.size();

		if (size >= (MAGICPEERNUMBER + peers)) { // put last peers to top
			for (int i = 1; i <= peers; i++) {
				peerList.add(0, peerList.get(size - 1));
				peerList.remove(size);
			}
			peerList.add(MAGICPEERNUMBER - 1, peerList.get(MAGICPEERNUMBER + peers - 1));
			peerList.remove(MAGICPEERNUMBER + peers);
		} else if (size >= MAGICPEERNUMBER + 1) {
			for (int i = MAGICPEERNUMBER; i <= size - 1; i++) {
				peerList.add(0, peerList.get(size - 1));
				peerList.remove(size);
			}
			peerList.add(MAGICPEERNUMBER - 1, peerList.get(size - 1));
			peerList.remove(size);
		} else
			checkAndUpdateInitialPeerListToMinumumSize();
	}

	/**
	 * puts resolving peer to top in list if exists already or adds newcomer to top few, but not to the very top
	 * 
	 * @param resolverAddress
	 */
	private void updatePeerList(InetSocketAddress resolverAddress) {
		if (peerList.contains(resolverAddress)) {
			peerList.remove(resolverAddress);
			peerList.add(0, resolverAddress); // put resolving peer to top
		} else {
			peerList.add(MAGICPEERNUMBER - 1, resolverAddress); // set newcomer to place MAGICPEERNUMBER-1 (eg 5) (included in next search as fix starter, but not to the top)
		}
	}

	private void logMessage(String log) {
		if (gui != null) {
			gui.setLog(log+newline);
			System.out.println(log);
		} else
			System.out.println(log);
	}

	/**
	 * output result to user
	 */
	private void displayResult() {
		logMessage("Title: " + message.getSongTitle());
		logMessage("Artist: " + message.getSongArtist());
	}

	/**
	 * if only demo file is found in config, method waits for user to input location of song snippet file and takes a fingerprint of the snippet Definitely WAV files are supported
	 * 
	 * @return fingerprint of sound snippet
	 * @throws SwazamException
	 */
	private Fingerprint getSnippetFileToFingerprintFromUser() throws SwazamException {
		String snippet = "";
		Fingerprint fingerprint = null;

		if (snippetFileName.equals(TESTFILE)) {
			do {
				try {
					// (record or) select music snippet file
					while (snippet == "") {
						System.out.println("Enter song snippet filename absolute (eg. " + TESTFILE + ") or enter for demo file: " + System.getProperty("user.dir") + snippetRootDirectory + TESTFILE);
						snippet = br.readLine();
					}
				} catch (IOException e) {
					System.err.println("Song snippet cannot be read. Standard filename '" + System.getProperty("user.dir") + snippetRootDirectory + TESTFILE + "' is used."); // exit alternatively
					snippet = System.getProperty("user.dir") + snippetRootDirectory + TESTFILE;
				}
				if (snippet == "") {
					snippet = System.getProperty("user.dir") + snippetRootDirectory + TESTFILE;
				}
				fingerprint = readFileAsFingerprint(snippet);
			} while (fingerprint == null);
		} else {
			logMessage("Snippet filename: '" + System.getProperty("user.dir") + snippetRootDirectory + snippetFileName + "' will be used.");
			fingerprint = readFileAsFingerprint(System.getProperty("user.dir") + snippetRootDirectory + snippetFileName);
		}
		return fingerprint;
	}

	public ClientCallback getClientCallback() {
		return this.clientCallback;
	}

	/**
	 * opening snippet file as audioInputStream and fingerprinting it with FingerprintTools
	 * 
	 * @param snippet
	 * @return audio fingerprint of sound file
	 * @throws SwazamException
	 */
	public Fingerprint readFileAsFingerprint(String snippet) throws SwazamException {
		File snippetFile = new File(snippet);
		Fingerprint fingerprint = null;
		logMessage("Snippet file found: " + snippetFile.exists());
		try {
			AudioInputStream snippetAudio = AudioSystem.getAudioInputStream(snippetFile);
			fingerprint = new FingerprintTools().generate(snippetAudio);
		} catch (UnsupportedAudioFileException | IOException e) {
			System.err.println("Audio format not supported or file could not be read.");
			e.printStackTrace();
		}
		return fingerprint;
	}

	/**
	 * user is logged in or an exception is thrown.
	 * 
	 * @return always true or exception
	 * @throws SwazamException
	 */
	private boolean performLogin() throws SwazamException {
		boolean loginSuccessful = false;
		int loginAttempt = 0;

		if (user.getUsername().equals(TESTDATA)) {
			logMessage("... but '" + TESTDATA + "' or other user needs to be entered manually.");
			do {
				String username = getUsernameFromUser();
				if (loginAttempt != 0) {
					logMessage("Please wait for " + (loginAttempt) * 500 + "ms to try to enter password again."); // render brute force unfeasable, avoid Server login DOS with user credentials except of default
				}
				try {
					Thread.sleep(loginAttempt * 500);
				} catch (InterruptedException e) {
					System.err.println("Maybe another client instance is still running?");
				} finally {
					loginAttempt++;
				}
				String password = getPasswortFromUser();
				loginSuccessful = login(username, password);
			} while (!loginSuccessful);
		} else {
			logMessage(user.getUsername() + " username will be used");
			loginSuccessful = serverStub.verifyCredentials(user);
		}
		return loginSuccessful;
	}

	/**
	 * stores the current peerlist locally, so the server does not have to be contacted again
	 * 
	 * @throws SwazamException
	 */
	public void shutdown() {
		logMessage("Shutting down client.");
		try {
			peerListBackup.storePeers(peerList);
		} catch (SwazamException e) {
			System.err.println("Could not store peer list.");
			e.printStackTrace();
		} finally {
			logMessage("Thank you for using SWAzam.");
			if (gui != null) {
				gui.shutdown();
			}
			System.exit(0);
		}
	}

	/**
	 * waits for user to input a password
	 * 
	 * @return entered password in clear text (or example password if anything went wrong)
	 */
	private String getPasswortFromUser() {
		String password;
		try {
			System.out.print("password: ");
			password = br.readLine();
		} catch (IOException e1) {
			System.err.println("Password cannot be read. Falling back to standard password: '" + TESTDATA + "'."); // exit alternatively
			password = TESTDATA;
		}
		return password;
	}

	/**
	 * waits for user to input a username
	 * 
	 * @return entered username (or example username if anything went wrong)
	 */
	private String getUsernameFromUser() {
		String username;
		try {
			System.out.print("username: ");
			username = br.readLine();
		} catch (IOException e1) {
			System.err.println("Username cannot be read. Falling back to standard username: '" + TESTDATA + "'."); // exit alternatively
			username = TESTDATA;
		}
		return username;
	}

	/**
	 * letting an answering peer set the message with a found song
	 * 
	 * @param answer
	 */
	public void setAnswer(MessageDTO answer) {
		// possibly check if UUID is the same
		this.message = answer;
	}

	public String getSnippetRootDirectory() {
		return snippetRootDirectory;
	}

	public int getClientPort() {
		return clientPort;
	}

	public InetSocketAddress getServerAddress() {
		return serverAddress;
	}

	/**
	 * Returns a snippet file name
	 * 
	 * @return soundSnippet File name string
	 */
	public String getSnippetFileName() {
		return this.snippetFileName;
	}

	/**
	 * returns storage path of peer list set in config file
	 * 
	 * @return string of / separated directory and file name
	 */
	public String getPeerListStoragePath() {
		return storagePath;
	}

	@Override
	public void updateProgress(int progress) {
		logMessage(".." + progress * 10 + "%");

		if (progress == 10) {
			// Time is over
			handleNoAnswer();
			try {
				tryAgain = getTrySnippetAgainDecisionFromUser();
			} catch (SwazamException e) {
				System.err.println("Server, internet connection, or database are down. Please try again later.");
				shutdown();
			}
		}
	}

	/**
	 * worst peers are removed from peer list, other Peers from peer list are added to Top to give them a chance next time. Is called when no answer was received (within 30 seconds)
	 */
	public void handleNoAnswer() {
		try {
			removePeersFromBottom(MAGICPEERNUMBER - 1); // reduce peerListSize from Bottom (peers did not pop up when returning results, and better ones did)
			addPeersToTop(MAGICPEERNUMBER - 1);// put MAGICPEERNUMBER-1 (eg 4) other peers from list to top (so best one from before still has a chance)
			logMessage(". Song snippet not found thifs time.");
		} catch (SwazamException e) {
			System.err.println("Server, internet connection, or database are down. Please try again later.");
			shutdown();
		}

	}

	/**
	 * if answer was received, abort the timer, log answer to server with resoving peer adress in message, update client peerlist and add resolving peer, and display result to user
	 * 
	 * @param answer
	 */
	public void handleAnswer(MessageDTO answer) {
		if (message != null) {
			message = answer;
			limiter.abort();
			try {
				serverStub.logRequest(user, message); // logRequest sends MessageDTO with completely filled out fields from first answering peer to server (server gives resolver a coin)
			} catch (SwazamException e) {
				System.err.println("Server, internet connection, or database are down. Please try again later.");
				shutdown();
			}
			updatePeerList(message.getResolverAddress()); // add resolving peer to peerlist

			displayResult(); // display result of peer to user
			if (gui != null) {
				tryAgain = getRepeatDecissionFromUser();
			}
			message = null;
		}
	}

	@Override
	public void finish() {}
}
