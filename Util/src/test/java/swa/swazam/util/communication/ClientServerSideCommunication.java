package swa.swazam.util.communication;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;

import swa.swazam.util.communication.api.ClientCommunicationUtil;
import swa.swazam.util.communication.api.CommunicationUtilFactory;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;

public class ClientServerSideCommunication {
	private ClientCommunicationUtil commUtil;

	protected MessageDTO answer;

	@Before
	public final void startup() throws Exception {
		answer = new MessageDTO(UUID.randomUUID(), "aSongTitle", "aSongArtist", new CredentialsDTO("thisIsMyName", ""));

		commUtil = CommunicationUtilFactory.createClientCommunicationUtil();
		commUtil.setCallback(mockServer());
		commUtil.startup();
	}

	@After
	public final void shutdown() {
		commUtil.shutdown();
	}

	private ClientCallback mockServer() throws Exception {
		ClientCallback callback = mock(ClientCallback.class);
		doNothing().when(callback).solved(answer);
		return callback;
	}

	public ClientCommunicationUtil getCommUtil() {
		return commUtil;
	}
}
