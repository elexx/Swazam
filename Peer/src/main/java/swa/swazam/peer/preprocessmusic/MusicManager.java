package swa.swazam.peer.preprocessmusic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.AudioSystem;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;

import swa.swazam.peer.PeerComponent;
import swa.swazam.peer.PeerController;
import swa.swazam.util.fingerprint.FingerprintTools;
import ac.at.tuwien.infosys.swa.audio.Fingerprint;

public class MusicManager implements PeerComponent {

	// uncomment if needed: private PeerController controller;

	private final HashMap<File, Fingerprint> fingerprints = new HashMap<File, Fingerprint>();

	private final HashMap<File, SongTag> tags = new HashMap<File, SongTag>();

	@Override
	public void setup(PeerController controller) {
		// uncomment if needed: this.controller = controller;
	}

	@Override
	public void destroy() {
		// for now: nop by design
	}

	public void scan(String musicRoot) throws FileNotFoundException {
		File root = new File(musicRoot);
		if (!root.exists()) throw new FileNotFoundException("Music root not found: " + musicRoot);
		for (File file : root.listFiles()) {
			Fingerprint fp;

			try {
				fp = new FingerprintTools().generate(AudioSystem.getAudioInputStream(file));
				fingerprints.put(file, fp);
			} catch (Exception ignored) {
				continue;
			}

			try {
				SongTag tag = createTag(file);
				tags.put(file, tag);
				fingerprints.put(file, fp);
			} catch (Exception ignoried) {
				continue;
			}
		}

		// TODO: remove debug output
		System.out.println("generated " + fingerprints.size() + " fingerprints");
	}

	public File match(Fingerprint sample) {
		for (File file : fingerprints.keySet()) {
			Fingerprint fingerprint = fingerprints.get(file);

			double match = fingerprint.match(sample);
			if (match == -1) continue;
			else return file;
		}

		return null;
	}

	private SongTag createTag(File file) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
		AudioFile audioFile = AudioFileIO.read(file);
		MP3File mp3File = (MP3File) audioFile;

		String title = mp3File.getID3v2TagAsv24().getFirst(FieldKey.TITLE).trim();
		String artist = mp3File.getID3v2TagAsv24().getFirst(FieldKey.ARTIST).trim();

		return new SongTag(title, artist);
	}

	public SongTag getTag(File file) {
		return tags.get(file);
	}

	public String getTitle(File f) {
		return getTag(f).getTitle();
	}

	public String getArtist(File f) {
		return getTag(f).getArtist();
	}
}
