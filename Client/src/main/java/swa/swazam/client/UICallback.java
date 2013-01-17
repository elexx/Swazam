package swa.swazam.client;

import java.util.UUID;

import swa.swazam.util.dto.MessageDTO;

public interface UICallback {

	/**
	 * will be called when a request was solved successfully. for a specific uuid either this or {@link UICallback#timedOut(UUID)} will be called.
	 * 
	 * @param message
	 */
	void solved(MessageDTO message);

	/**
	 * will be called when a request timed out. for a specific uuid either this or {@link UICallback#solved(MessageDTO)} will be called.
	 * 
	 * @param uuid
	 */
	void timedOut(UUID uuid);

}
