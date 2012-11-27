package swa.swazam.util.communication;

import java.net.InetSocketAddress;
import swa.swazam.util.dto.MessageDTO;

public interface Peer2Client {
	
	/**
	 * Sends the identified song information to the specified client.
	 * @param answer the song information containing the resolver
	 * @param client the client to which the answer should be delivered
	 */
	public void solved(MessageDTO answer, InetSocketAddress client);

}
