package swa.swazam.util.communication.api;

import swa.swazam.util.communication.api.intern.dto.ResponseWirePacket;

interface Notifiable {

	/**
	 * injects a response and notifies the waiting thread about it
	 * 
	 * @param id
	 * @param packet
	 */
	public void notifyId(int id, ResponseWirePacket packet);

}
