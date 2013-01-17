package swa.swazam.peer.requestmanagement;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;

import swa.swazam.peer.PeerComponent;
import swa.swazam.peer.PeerController;
import swa.swazam.peer.preprocessmusic.MusicManager;
import swa.swazam.peer.preprocessmusic.SongTag;
import swa.swazam.util.dto.RequestDTO;

public class RequestManager implements PeerComponent {

	private PeerController controller;

	private Hashtable<UUID, File> matches = new Hashtable<>();
	private Set<UUID> handled = new HashSet<>();

	@Override
	public void setup(PeerController controller) {
		this.controller = controller;
	}

	@Override
	public void destroy() {
		// for now: nop by design
	}

	public void process(RequestDTO request) {
		if (handled.contains(request.getUuid())) {
			System.out.println("[debug] dup " + request.getUuid());
			return;
		}
		handled.add(request.getUuid());

		if (request.getTtl() <= 0) {
			System.out.println("[debug] drop-ttl " + request.getUuid());
			return;
		}

		if (request.getTimer() <= 0) {
			System.out.println("[debug] drop-time " + request.getTimer());
		}

		long timeStamp = System.currentTimeMillis();
		MusicManager mm = controller.getMusicManager();

		File match = null;
		if (matches.contains(request.getUuid())) {
			match = matches.get(request.getUuid());
		} else {
			match = mm.match(request.getFingerprint());
		}
		SongTag tag = match == null ? null : mm.getTag(match);

		if (match == null) {
			if (request.getTtl() == 0 || request.getTimer() < (short) (System.currentTimeMillis() - timeStamp)) return;
			request.setTtl((short) (request.getTtl() - 1));
			request.setTimer(request.getTimer() - (short) (System.currentTimeMillis() - timeStamp));
			controller.forwardRequest(request);
		} else {
			controller.solveRequest(request, tag.getArtist(), tag.getTitle());
		}

		System.out.println("[debug] processed " + request.getUuid());
	}
}
