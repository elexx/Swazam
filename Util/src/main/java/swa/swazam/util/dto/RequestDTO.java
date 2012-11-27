package swa.swazam.util.dto;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * This class is used to send a request from the client to the peers
 */
public class RequestDTO {

	private UUID uuid;
	private InetSocketAddress client;
	private String fingerprint; //TODO
	/**
	 * Has to be decreased by one before! the request is forwarded
	 */
	private short ttl; 
	/**
	 * Has to be decreased by the time consumed during operation before! the request is forwarded
	 * Time is defined in milli seconds.
	 */
	private long timer;
	
	public RequestDTO(UUID uuid, InetSocketAddress client, String fingerprint) {
		super();
		this.uuid = uuid;
		this.client = client;
		this.fingerprint = fingerprint;
		this.timer = 30000;
		this.ttl = 5;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public InetSocketAddress getClient() {
		return client;
	}

	public void setClient(InetSocketAddress client) {
		this.client = client;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public short getTtl() {
		return ttl;
	}

	public void setTtl(short ttl) {
		this.ttl = ttl;
	}

	public long getTimer() {
		return timer;
	}

	public void setTimer(long timer) {
		this.timer = timer;
	}

	@Override
	public String toString() {
		return "RequestDTO [uuid=" + uuid + ", client=" + client
				+ ", fingerprint=" + fingerprint + ", ttl=" + ttl + ", timer="
				+ timer + "]";
	}
}
