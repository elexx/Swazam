package swa.swazam.client;

import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.dto.MessageDTO;

public class ClientCallbackImpl implements ClientCallback {

	private ClientApp app;

	public ClientCallbackImpl(ClientApp app) {
		this.app = app;
	}

	@Override
	public void solved(MessageDTO answer) {
		app.handleAnswer(answer);
		
		System.err.println(answer.getSongTitle());
	}
}
