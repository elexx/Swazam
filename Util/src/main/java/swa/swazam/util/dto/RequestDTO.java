package swa.swazam.util.dto;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.UUID;

import ac.at.tuwien.infosys.swa.audio.Fingerprint;

/**
 * This class is used to send a request from the client to the peers
 */
public class RequestDTO implements Serializable {
	private static final long serialVersionUID = -4128277394441822513L;

	private UUID uuid;
	private InetSocketAddress client;
	private Fingerprint fingerprint;

	/**
	 * Has to be decreased by one before! the request is forwarded
	 */
	private short ttl;

	/**
	 * Has to be decreased by the time consumed during operation before! the request is forwarded Time is defined in milliseconds.
	 */
	private long timer;

	public RequestDTO(UUID uuid, InetSocketAddress client, Fingerprint fingerprint) {
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

	public Fingerprint getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(Fingerprint fingerprint) {
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
		return "RequestDTO [uuid=" + uuid + ", client=" + client + ", fingerprint=" + fingerprint.toString() + ", ttl=" + ttl + ", timer=" + timer + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((client == null) ? 0 : client.hashCode());
		result = prime * result + ((fingerprint == null) ? 0 : fingerprint.hashCode());
		result = prime * result + (int) (timer ^ (timer >>> 32));
		result = prime * result + ttl;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestDTO other = (RequestDTO) obj;
		if (client == null) {
			if (other.client != null)
				return false;
		} else if (!client.equals(other.client))
			return false;
		if (fingerprint == null) {
			if (other.fingerprint != null)
				return false;
		} else if (!fingerprint.equals(other.fingerprint))
			return false;
		if (timer != other.timer)
			return false;
		if (ttl != other.ttl)
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
}
