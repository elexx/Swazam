package swa.swazam.peer.preprocessmusic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.logging.LogManager;

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

public class MusicManager implements PeerComponent, Runnable {

	private PeerController controller;

	private HashMap<File, Fingerprint> fingerprints = new HashMap<File, Fingerprint>();
	private HashMap<File, SongTag> tags = new HashMap<File, SongTag>();

	private File musicRoot;

	private Thread musicManagerThread;
	private boolean cancel = false;

	@Override
	public void setup(PeerController controller) {
		try {
			LogManager.getLogManager().readConfiguration(new ByteArrayInputStream("org.jaudiotagger.level = OFF".getBytes()));
		} catch (SecurityException | IOException e) {
			// tough luck
		}

		this.controller = controller;

		musicManagerThread = new Thread(this);
		musicManagerThread.setName("Music library watcher");
		musicManagerThread.setDaemon(true);
	}

	@Override
	public void destroy() {
		cancel = true;
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
		if (!tags.containsKey(file)) {
			System.err.println("warning: file \"" + file.toString() + "\" not found in internal taglist");
		}
		return tags.get(file);
	}

	public String getTitle(File f) {
		return getTag(f).getTitle();
	}

	public String getArtist(File f) {
		return getTag(f).getArtist();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		while (!cancel) {
			WatchService watcher;
			try {
				watcher = FileSystems.getDefault().newWatchService();

				Path musicRootP = musicRoot.toPath();
				musicRootP.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
			} catch (IOException e) {
				System.err.println("error setting up watcher");
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

			// inner loop is needed because the watcher key can be invalidated
			while (!cancel) {
				WatchKey key;

				try {
					key = watcher.take();
				} catch (InterruptedException iEx) {
					break;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					if (kind == StandardWatchEventKinds.OVERFLOW) continue;
					File file = new File(musicRoot.getAbsolutePath() + File.separator + ((WatchEvent<Path>) event).context().toString());

					fingerprints.remove(file);
					tags.remove(file);
					checkFile(file);
				}

				try {
					persistData();
				} catch (IOException e2) {
					// TODO: nicer output/logging?
					System.err.println("music data persistence failed");
					e2.printStackTrace();
				}

				boolean valid = key.reset();
				if (!valid) {
					break;
				}
			}
		}
	}

	public void setRootPath(String root) throws FileNotFoundException {
		musicRoot = new File(root);
		if (!musicRoot.exists()) throw new FileNotFoundException("Music root not found: " + musicRoot);
	}

	public void initialScan() {
		if (musicRoot == null) return;
		readData();

		for (File file : musicRoot.listFiles()) {
			checkFile(file);
		}
	}

	@SuppressWarnings("unchecked")
	private void readData() {
		try {
			File persistFile = new File(controller.getStorageRoot() + File.separator + "tags");
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(persistFile));
			try {
				fingerprints = (HashMap<File, Fingerprint>) ois.readObject();
				tags = (HashMap<File, SongTag>) ois.readObject();
			} finally {
				ois.close();
			}
		} catch (IOException | ClassNotFoundException ignored) {}
	}

	public void persistData() throws FileNotFoundException, IOException {
		File persistFile = new File(controller.getStorageRoot() + File.separator + "tags");
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(persistFile));
		try {
			oos.writeObject(fingerprints);
			oos.writeObject(tags);
		} finally {
			oos.close();
		}
	}

	private void checkFile(File file) {
		Fingerprint fp;

		if (!fingerprints.containsKey(file)) {
			try {
				fp = new FingerprintTools().generate(AudioSystem.getAudioInputStream(file));
			} catch (Exception ignored) {
				return;
			}
		} else fp = fingerprints.get(file);

		if (!tags.containsKey(file)) {
			try {
				SongTag tag = createTag(file);
				tags.put(file, tag);
				fingerprints.put(file, fp);
				System.out.println("Tag for " + file.toString() + " generated");
			} catch (Exception ignored) {
				return;
			}
		}
	}

	public void startWatcher() {
		musicManagerThread.start();
	}
}
