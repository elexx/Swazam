package swa.swazam.util.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Is used to send logging information from the client to the server
 * and to send request answers from a peer to the client.
 *
 */
public class MessageDTO implements Serializable{
	private static final long serialVersionUID = -6471166539293823671L;
	
	private UUID uuid;
	private String songTitle;
	private String songArtist;
	private CredentialsDTO resolver;
	
	public MessageDTO(UUID uuid, String songTitle, String songArtist,
			CredentialsDTO resolver) {
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

	@Override
	public String toString() {
		return "MessageDTO [uuid=" + uuid + ", songTitle=" + songTitle
				+ ", songArtist=" + songArtist + ", resolver=" + resolver + "]";
	}
}
