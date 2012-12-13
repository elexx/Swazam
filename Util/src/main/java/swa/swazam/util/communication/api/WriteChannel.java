package swa.swazam.util.communication.api;

interface WriteChannel {
	/**
	 * writes message to the underlying channel
	 * 
	 * @param message
	 */
	public void write(Object message);
}
