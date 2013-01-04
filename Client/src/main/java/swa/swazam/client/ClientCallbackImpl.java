package swa.swazam.client;

import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.dto.MessageDTO;

public class ClientCallbackImpl implements ClientCallback {

	@Override
	public void solved(MessageDTO answer) {
		// TODO Auto-generated method stub
		
//		wir haben deshalb ins messagedto noch eine inetsockaddress
//		hinzugefügt, diese wird vom commutil beim senden vom peer zum client
//		gesetzt (also der peer selbst braucht da nix machen). wichtig beim
//		client: entweder das per callback empfangene messagedto an den server
//		weiterleiten, oder falls ein neues erstellt wird die address vom
//		empfangenen ins neue kopieren!!
		
		
		// logRequest sends MessageDTO with completely filled out fields from first answering peer to server
		
		// display result of peer to user
		
		 
	}

}
