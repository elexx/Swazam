package swa.swazam.clientnew;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.communication.api.ClientCommunicationUtil;
import swa.swazam.util.communication.api.CommunicationUtilFactory;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.dto.RequestDTO;
import swa.swazam.util.exceptions.SwazamException;

public class ClientLogic implements ClientCallback, LogicCallback {

	private final ClientCommunicationUtil commUtil;
	private final UICallback uiCallback;

	private final Map<UUID, Long> pendingRequests = Collections.synchronizedMap(new HashMap<UUID, Long>());

	private CredentialsDTO credentials;

	public ClientLogic(InetSocketAddress serverAddress, UICallback uiCallback) throws SwazamException {
		this.uiCallback = uiCallback;

		commUtil = CommunicationUtilFactory.createClientCommunicationUtil(serverAddress);
		commUtil.setCallback(this);
		commUtil.startup();
	}

	@Override
	public void solved(MessageDTO answer) {
		uiCallback.solved(answer);
	}

	@Override
	public boolean login(CredentialsDTO credentials) throws SwazamException {
		if (commUtil.getServerStub().verifyCredentials(credentials)) {
			this.credentials = credentials;
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

		UUID uuid = UUID.randomUUID();
		
		MessageDTO message = new MessageDTO(uuid, null, null, null);
		commUtil.getServerStub().logRequest(credentials, message);
		pendingRequests.put(uuid, System.currentTimeMillis());
		
		// TODO 3rd param is fingerprint, not null!!
		RequestDTO request = new RequestDTO(uuid, null, null);

		// TODO 2nd param is top5 peers, not null!!
		commUtil.getPeerStub().process(request, null);

		return uuid;
	}

	private void checkCredentials() throws SwazamException {
		if (null == credentials) {
			throw new SwazamException("no credentials set");
		}
	}
}
