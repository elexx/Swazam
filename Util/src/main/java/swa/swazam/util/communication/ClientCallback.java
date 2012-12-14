package swa.swazam.util.communication;

import swa.swazam.util.dto.MessageDTO;

public interface ClientCallback {

	/**
	 * Handles the received answer of the identified song
	 * 
	 * @param answer the song information containing the resolver
	 */
	public void solved(MessageDTO answer);

}
