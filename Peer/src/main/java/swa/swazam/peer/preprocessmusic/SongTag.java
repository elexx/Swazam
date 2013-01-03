package swa.swazam.peer.preprocessmusic;

public class SongTag {
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
