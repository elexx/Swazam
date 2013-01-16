package swa.swazam.client;

import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.dto.MessageDTO;

public class ClientCallbackImpl implements ClientCallback {

	private ClientApp app;

	public ClientCallbackImpl(ClientApp app) {
		this.app = app;
	}

	@Override
	public void solved(final MessageDTO answer) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				app.handleAnswer(answer);
			}
		}).start();
		
		System.err.println(answer.getSongTitle());
	}
}
