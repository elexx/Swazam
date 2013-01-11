package swa.swazam.client;

import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.dto.MessageDTO;

public class ClientCallbackImpl implements ClientCallback {

	private App app;

	public ClientCallbackImpl(App app) {
		this.app = app;
	}

	@Override
	public void solved(MessageDTO answer) {
		app.setAnswer(answer);
		this.notify();
	}
}
