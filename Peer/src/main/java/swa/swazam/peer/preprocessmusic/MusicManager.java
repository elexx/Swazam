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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

	private HashMap<String, Fingerprint> fingerprints = new HashMap<String, Fingerprint>();
	private HashMap<String, SongTag> tags = new HashMap<String, SongTag>();

	private final long CHECK_DELAY = 200;
	private Map<File, Long> toCheck = new HashMap<File, Long>();

	private Set<File> files = new HashSet<>();

	private File musicRoot;

	private Thread musicWatcherThread;
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

		musicWatcherThread = new Thread(new Runnable() {
			@Override
			public void run() {
				watchLoop();
			}
		});
		musicWatcherThread.setName("Music library watcher");
		musicWatcherThread.setDaemon(true);

		musicManagerThread = new Thread(this);
		musicManagerThread.setName("Music library watcher");
		musicManagerThread.setDaemon(true);
	}

	@Override
	public void destroy() {
		cancel = true;
	}

	public File match(Fingerprint sample) {
		for (File file : files) {
			System.out.println("[debug] matching " + file.getName() + "...");
			Fingerprint fingerprint = fingerprints.get(file.getName());

			double match = fingerprint.match(sample);
			System.out.println("[debug] got " + match);
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
		return tags.get(file.getName());
	}

	public String getTitle(File f) {
		return getTag(f).getTitle();
	}

	public String getArtist(File f) {
		return getTag(f).getArtist();
	}

	@Override
	public void run() {
		long sleepNeeded;
		while (true) {
			sleepNeeded = 10000;

			synchronized (toCheck) {
				Set<File> toRemove = new HashSet<File>();
				for (File file : toCheck.keySet()) {
					if (toCheck.get(file) < System.currentTimeMillis()) {
						toRemove.add(file);
						checkFile(file);
					} else sleepNeeded = Math.min(sleepNeeded, toCheck.get(file) - System.currentTimeMillis());
				}
				for (File file : toRemove) {
					toCheck.remove(file);
				}
			}

			try {
				persistData();
			} catch (IOException e2) {
				// TODO: nicer output/logging?
				System.err.println("music data persistence failed");
				e2.printStackTrace();
			}

			try {
				Thread.sleep(sleepNeeded + 200);
			} catch (InterruptedException ignored) {}
		}
	}

	@SuppressWarnings("unchecked")
	private void watchLoop() {
		while (!cancel) {
			WatchService watcher;
			try {
				watcher = FileSystems.getDefault().newWatchService();

				Path musicRootP = musicRoot.toPath();
				musicRootP.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
			} catch (IOException e) {
				System.err.println("error setting up watcher");
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

					synchronized (toCheck) {
						toCheck.put(file, System.currentTimeMillis() + CHECK_DELAY);
					}
				}

				musicManagerThread.interrupt();

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
				fingerprints = (HashMap<String, Fingerprint>) ois.readObject();
				tags = (HashMap<String, SongTag>) ois.readObject();
			} finally {
				ois.close();
			}
		} catch (IOException | ClassNotFoundException | ClassCastException ignored) {}
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

		if (!file.exists()) {
			files.remove(file);
			return;
		}

		files.add(file);

		if (!fingerprints.containsKey(file.getName())) {
			try {
				fp = new FingerprintTools().generate(AudioSystem.getAudioInputStream(file));
			} catch (Exception ignored) {
				return;
			}
		} else fp = fingerprints.get(file.getName());

		if (!tags.containsKey(file.getName())) {
			try {
				SongTag tag = createTag(file);
				tags.put(file.getName(), tag);
				fingerprints.put(file.getName(), fp);
				System.out.println("Tag for " + file.toString() + " generated");
			} catch (Exception ignored) {
				return;
			}
		} else System.out.println("Tag for " + file.toString() + " generated (from cache)");
	}

	public void startWatcher() {
		musicManagerThread.start();
		musicWatcherThread.start();
	}
}
