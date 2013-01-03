package swa.swazam.util.dto;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * Is used to send logging information from the client to the server and to send request answers from a peer to the client.
 */
public class MessageDTO implements Serializable {
	private static final long serialVersionUID = -6471166539293823671L;

	private UUID uuid;
	private String songTitle;
	private String songArtist;
	private CredentialsDTO resolver;
	private InetSocketAddress resolverAddress;

	public MessageDTO(UUID uuid, String songTitle, String songArtist, CredentialsDTO resolver) {
		this.uuid = uuid;
		this.songTitle = songTitle;
		this.songArtist = songArtist;
		this.resolver = resolver;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getSongTitle() {
		return songTitle;
	}

	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}

	public String getSongArtist() {
		return songArtist;
	}

	public void setSongArtist(String songArtist) {
		this.songArtist = songArtist;
	}

	public CredentialsDTO getResolver() {
		return resolver;
	}

	public void setResolver(CredentialsDTO resolver) {
		this.resolver = resolver;
	}

	public InetSocketAddress getResolverAddress() {
		return resolverAddress;
	}

	public void setResolverAddress(InetSocketAddress resolverAddress) {
		this.resolverAddress = resolverAddress;
	}

	@Override
	public String toString() {
		return "MessageDTO [uuid=" + uuid + ", songTitle=" + songTitle + ", songArtist=" + songArtist + ", resolver=" + resolver + ", resolverAddress=" + resolverAddress + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resolver == null) ? 0 : resolver.hashCode());
		result = prime * result + ((resolverAddress == null) ? 0 : resolverAddress.hashCode());
		result = prime * result + ((songArtist == null) ? 0 : songArtist.hashCode());
		result = prime * result + ((songTitle == null) ? 0 : songTitle.hashCode());
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
		MessageDTO other = (MessageDTO) obj;
		if (resolver == null) {
			if (other.resolver != null)
				return false;
		} else if (!resolver.equals(other.resolver))
			return false;
		if (resolverAddress == null) {
			if (other.resolverAddress != null)
				return false;
		} else if (!resolverAddress.equals(other.resolverAddress))
			return false;
		if (songArtist == null) {
			if (other.songArtist != null)
				return false;
		} else if (!songArtist.equals(other.songArtist))
			return false;
		if (songTitle == null) {
			if (other.songTitle != null)
				return false;
		} else if (!songTitle.equals(other.songTitle))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
}
