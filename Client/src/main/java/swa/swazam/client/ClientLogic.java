package swa.swazam.client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.communication.api.ClientCommunicationUtil;
import swa.swazam.util.communication.api.CommunicationUtilFactory;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.dto.RequestDTO;
import swa.swazam.util.exceptions.SwazamException;
import swa.swazam.util.fingerprint.FingerprintTools;
import swa.swazam.util.hash.HashGenerator;
import ac.at.tuwien.infosys.swa.audio.Fingerprint;

public class ClientLogic implements ClientCallback, LogicCallback {

	private final ClientCommunicationUtil commUtil;
	private final UICallback uiCallback;

	private final Map<UUID, Long> pendingRequests = new HashMap<>();

	private CredentialsDTO credentials;

	private boolean shutdown = false;

	public ClientLogic(InetSocketAddress serverAddress, UICallback uiCallback) throws SwazamException {
		this.uiCallback = uiCallback;

		commUtil = CommunicationUtilFactory.createClientCommunicationUtil(serverAddress);
		commUtil.setCallback(this);
		commUtil.startup();

		Thread watcher = new Thread(new Runnable() {
			@Override
			public void run() {
				watcherThread();
			}
		});
		watcher.setDaemon(true);
		watcher.start();
	}

	public ClientLogic(Properties config, ParameterSet params, ClientUI ui) throws SwazamException {
		this(extractInetSocketAddress(config), ui);
	}

	private static InetSocketAddress extractInetSocketAddress(Properties config) throws InsufficientParametersException {
		if (!config.containsKey(ClientApp.CONFIG_HOSTNAME) || !config.containsKey(ClientApp.CONFIG_PORT)) throw new InsufficientParametersException(
				"Hostname and/or port are missing");

		int port;
		try {
			port = Integer.parseInt(config.getProperty(ClientApp.CONFIG_PORT));
		} catch (NumberFormatException nfEx) {
			throw new InsufficientParametersException("Provided port is not an integer", nfEx);
		}

		return new InetSocketAddress(config.getProperty(ClientApp.CONFIG_HOSTNAME), port);
	}

	@Override
	public void solved(final MessageDTO answer) {
		synchronized (pendingRequests) {
			if (pendingRequests.containsKey(answer.getUuid())) {
				pendingRequests.remove(answer.getUuid());
				uiCallback.solved(answer);

				notifyServer(answer);
			}
		}
	}

	@Override
	public boolean login(CredentialsDTO credentials) throws SwazamException {
		CredentialsDTO encrypted = new CredentialsDTO(credentials.getUsername(), HashGenerator.hash(credentials.getPassword()));
		if (commUtil.getServerStub().verifyCredentials(encrypted)) {
			this.credentials = encrypted;
			return true;
		} else {
			this.credentials = null;
			return false;
		}
	}

	@Override
	public boolean isLoggedIn() {
		return null != credentials;
	}

	@Override
	public boolean hasCoins() throws SwazamException {
		checkCredentials();
		return commUtil.getServerStub().hasCoins(credentials);
	}

	@Override
	public UUID fileChosen(File selectedFile) throws SwazamException {
		checkCredentials();

		Fingerprint fingerprint = generateFingerprint(selectedFile);

		UUID uuid = UUID.randomUUID();

		MessageDTO message = new MessageDTO(uuid, null, null, null);
		commUtil.getServerStub().logRequest(credentials, message);
		synchronized (pendingRequests) {
			pendingRequests.put(uuid, System.currentTimeMillis());
		}

		RequestDTO request = new RequestDTO(uuid, null, fingerprint);
		List<InetSocketAddress> peerList = commUtil.getServerStub().getPeerList();

		synchronized (pendingRequests) {
			pendingRequests.put(uuid, System.currentTimeMillis() + RequestDTO.TIMEOUT);
		}

		commUtil.getPeerStub().process(request, peerList);

		return uuid;
	}

	@Override
	public void shutdown() {
		shutdown = true;
		synchronized (pendingRequests) {
			pendingRequests.clear();
		}
		commUtil.shutdown();
	}

	private Fingerprint generateFingerprint(File selectedFile) throws SwazamException {
		AudioInputStream snippetAudio = null;
		try {
			snippetAudio = AudioSystem.getAudioInputStream(selectedFile);
			return new FingerprintTools().generate(snippetAudio);
		} catch (SwazamException e) {
			throw e;
		} catch (UnsupportedAudioFileException | IOException e) {
			throw new SwazamException(e);
		} finally {
			if (snippetAudio != null) try {
				snippetAudio.close();
			} catch (IOException ignored) {}
		}
	}

	private void checkCredentials() throws SwazamException {
		if (null == credentials) {
			throw new SwazamException("no credentials set");
		}
	}

	private void watcherThread() {
		long sleepNeeded;
		while (!shutdown) {
			sleepNeeded = RequestDTO.TIMEOUT;

			Set<UUID> toRemove = new HashSet<UUID>();
			synchronized (pendingRequests) {
				for (UUID uuid : pendingRequests.keySet()) {
					if (pendingRequests.get(uuid) < System.currentTimeMillis()) {
						uiCallback.timedOut(uuid);
						toRemove.add(uuid);
					} else sleepNeeded = Math.min(sleepNeeded, pendingRequests.get(uuid) - System.currentTimeMillis() + 10);
				}

				for (UUID uuid : toRemove)
					pendingRequests.remove(uuid);
			}

			try {
				Thread.sleep(sleepNeeded);
			} catch (InterruptedException ignored) {}
		}
	}

	private void notifyServer(final MessageDTO answer) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					commUtil.getServerStub().logRequest(credentials, answer);
				} catch (SwazamException e) {
					e.printStackTrace();
				}
			};
		}).start();
	}
}
