package swa.swazam.client;

import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.dto.MessageDTO;

public class ClientCallbackImpl implements ClientCallback {
	
	private MessageDTO answer;

	@Override
	public void solved(MessageDTO answer) {
		this.answer = answer;
		this.notify();			 
	}

	public MessageDTO getAnswer() {
		return answer;
	}

}
