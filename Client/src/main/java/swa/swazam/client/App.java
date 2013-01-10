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

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

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
public class App {

	private static final String TESTDATA = "demo";
	private static final String TESTFILE = TESTDATA + ".mp3";
	private static final int MAGICPEERNUMBER = 5; // has to be at least 2

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

	public App() {
		peerList = new ArrayPeerList<>();
		clientCallback = new ClientCallbackImpl(this);
		br = new BufferedReader(new InputStreamReader(System.in));

		// requestManager = new RequestManager();
		// requestManager.setup(this);
	}

	public static void main(String[] args) {
		// TODO read args from startup (property file name)
		App app = new App();
		System.out.println(app.getClass().getClassLoader().getResourceAsStream("client.properties"));
		welcomeMessage();
		app.run();
	}

	/**
	 * prints a welcome message for the user
	 */
	private static void welcomeMessage() {
		System.out.println("Welcome to SWAzam!\n");
		System.out.println("To try out our service, we registered already a testuser with a number of coins");
		System.out.println("Login and password are: demo\n");
		System.out.println("A test recording, containing a 7 second random song snippet we provide as well: demo.mp3");
		System.out.println("Have fun with SWAzam!");
	}

	public void run() {
		try {
			loadConfig();
		} catch (IOException e1) {
			System.err.println("Loading config was not possible.");
			System.exit(0);
		}
		try {
			setupCommLayer();
		} catch (SwazamException e1) {
			System.err.println("Communication setup failed.");
			System.exit(0);
		}
		try {
			setupStorage();
		} catch (SwazamException e1) {
			System.err.println("Local peer list not found. Attempting Server.");
		}
		try {
			performLogin(); // let user enter username and password on commandline if config file contains testdata
			searchForSnippet();
		} catch (SwazamException e) {
			System.err.println("Server, internet connection, or database are down. Please try again later.");
			System.exit(0);
		}
		try {
			shutdown();
		} catch (SwazamException e) {
			System.err.println("Could not store peer list.");
			System.exit(0);
		}
	}

	/**
	 * reads configuration (server address, client port for peer callback, snippet root directory)
	 * 
	 * @throws IOException
	 */
	protected void loadConfig() throws IOException {
		Properties configFile = new Properties();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("client.properties");
		//System.out.println("inputstream is " + is);
		configFile.load(is);

		String username = configFile.getProperty("credentials.user");
		String password = configFile.getProperty("credentials.pass");
		System.out.println("testuser: " + username + " and password: "+ password +" found in config.");
		user = new CredentialsDTO(username, HashGenerator.hash(password));

		String serverHostname = configFile.getProperty("server.hostname");
		int serverPort = Integer.parseInt(configFile.getProperty("server.port"));
		serverAddress = new InetSocketAddress(Inet4Address.getByName(serverHostname), serverPort);

		clientPort = Integer.parseInt(configFile.getProperty("client.port"));
		clientSocketAddress = new InetSocketAddress(Inet4Address.getLocalHost().getHostAddress(), clientPort);

		snippetRootDirectory = configFile.getProperty("snippet.root");

		storagePath = configFile.getProperty("peerlist.storagepath");
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

	protected void setupCommLayer() throws SwazamException {
		commLayer = swa.swazam.util.communication.api.CommunicationUtilFactory.createClientCommunicationUtil(serverAddress); // clientPort
		commLayer.setCallback(clientCallback);
		commLayer.startup();

		peerStub = commLayer.getPeerStub();
		serverStub = commLayer.getServerStub();
	}

	protected void setupStorage() throws SwazamException {
		peerListBackup = new PeerListBackup(storagePath);
		peerList.addAll(peerListBackup.loadPeers());
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
	}

	/**
	 * check if user has coins to do a search, generate a fingerprint, send to peers from peerlist, and displays result
	 * 
	 * @throws SwazamException
	 */
	private void searchForSnippet() throws SwazamException {
		boolean hasCoins = false;
		Fingerprint fingerprint = null;
		boolean tryAgain = true;

		hasCoins = checkforCoins();
		if (!hasCoins) {
			System.out.println("No more coins available. You receive coins when your peer program identifies a song request of other clients.");
			System.err.println("Client quitting because of no coins");
			return;
		}

		fingerprint = generateFingerprintForSnippet();

		while (tryAgain) {
			createRequest(fingerprint); // create UUID for RequestDTO, create MessageDTO with UUID filled out already
			serverStub.logRequest(user, message); // logRequest sends UUID/MessageDTO to Server
			checkAndUpdateInitialPeerListToMinumumSize();

			// send MessageDTO (with UUID fingerprint) to top MAGICPEERNUMBER (eg.5) peers from client peerlist in parallel
			// eventually also receive a return value of a successful or failed connection attempt to first peers, so that more peers in list can be tried without the request failing after first MAGICPEERNUMBER (eg 5) not online
			peerStub.process(request, peerList.getTop(MAGICPEERNUMBER));

			// wait 30 seconds, if no answer, display to user, "song snippet not found, try again later?/discard"
			System.out.print("searching");
			for (int i = 0; i <= 10; i++) {
				System.out.print(".." + (i * 10) + "%");
				try {					
					synchronized (clientCallback) {
						clientCallback.wait(3000);	
					}
					
				} catch (InterruptedException e) {
					serverStub.logRequest(user, message); // logRequest sends MessageDTO with completely filled out fields from first answering peer to server (server gives resolver a coin)

					updatePeerList(message.getResolverAddress());

					displayResult(); // display result of peer to user
					return;
				}
			}
			removePeersFromBottom(MAGICPEERNUMBER - 1); // reduce peerListSize from Bottom (peers did not pop up when returning results, and better ones did)
			addPeersToTop(MAGICPEERNUMBER - 1);// put MAGICPEERNUMBER-1 (eg 4) other peers from list to top (so best one from before still has a chance)

			tryAgain = false;
			System.out.println(". Song snippet not found this time.");

			hasCoins = checkforCoins();

			String answer = "d";

			if (hasCoins) {
				System.out.println("Try again (a)? or Discard (d) and start new search or Quit (q)? [a|(d)|q]:"); // search again with different top MAGICPEERNUMBER (eg 5) or discard and loop back to hascoins check

				try {
					answer = br.readLine();
					if (answer.equalsIgnoreCase("a")) {
						tryAgain = true;
					}
					else if (answer.equalsIgnoreCase("q")){
						tryAgain = false;
					}						
				} catch (IOException e) {
					System.err.println("Could not read input, opting for default answer: Discard");
				}
			}
		}
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

	/**
	 * output result to user
	 */
	private void displayResult() {
		System.out.println("Title: " + message.getSongTitle());
		System.out.println("Artist: " + message.getSongArtist());
	}

	/**
	 * waits for user to input location of song snippet file and takes a fingerprint of the snippet
	 * 
	 * @return fingerprint of sound snippet
	 * @throws SwazamException
	 */
	private Fingerprint generateFingerprintForSnippet() throws SwazamException {
		String snippet;
		Fingerprint fingerprint = null;
		do {
			try {
				// (record or) select music snippet file
				System.out.print("song snippet filename: ");
				snippet = br.readLine();
				System.out.println(snippet + " will be used");
			} catch (IOException e) {
				System.err.println("Song snippet cannot be read. Standard filename '" + TESTDATA + ".mp3' is used."); // exit alternatively
				snippet = snippetRootDirectory + TESTFILE;
			}

			File snippetFile = new File(snippet);
			System.out.println(snippetFile.exists());
			try {
				fingerprint = new FingerprintTools().generate(AudioSystem.getAudioInputStream(snippetFile));
			} catch (UnsupportedAudioFileException | IOException e) {
				System.err.println("Audio format not supported or file could not be read.");
				System.err.println(e.toString());
			}
		} while (fingerprint == null);

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

		if (user.getUsername().trim().equals(TESTDATA.toString().trim())) {
			do {
				String username = getUsernameFromUser();
				if (loginAttempt != 0) {
					System.out.println("please wait for " + (loginAttempt) * 500 + "ms to enter password."); // render brute force unfeasable, avoid Server login DOS with user credentials except of default
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
		}
		else {
			System.out.println(user.getUsername() + " username will be used");
				loginSuccessful = serverStub.verifyCredentials(user);
		}
		return loginSuccessful;
	}

	/**
	 * stores the current Peerlist locally, so the server does not have to be contacted again
	 * 
	 * @throws SwazamException
	 */
	private void shutdown() throws SwazamException {
		peerListBackup.storePeers(peerList);
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
			System.err.println("Password cannot be read. Falling back to standard password '" + TESTDATA + "'."); // exit alternatively
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
			System.err.println("Username cannot be read. Falling back to standard username '" + TESTDATA + "'."); // exit alternatively
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
}
