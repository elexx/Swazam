package swa.swazam.util.communication.api;

interface Notifiable {

	/**
	 * injects a response and notifies the waiting thread about it
	 * 
	 * @param id
	 * @param packet
	 */
	void notifyId(int id, ResponseWirePacket packet);

}
