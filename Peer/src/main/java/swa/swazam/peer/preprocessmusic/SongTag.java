package swa.swazam.peer.preprocessmusic;

import java.io.Serializable;

public class SongTag implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1926779608643294644L;

	private final String artist, title;

	public SongTag(String title, String artist) {
		this.artist = artist;
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public String getTitle() {
		return title;
	}
}
