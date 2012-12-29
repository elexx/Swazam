package swa.swazam.peer.requestmanagement;

import java.io.File;

import swa.swazam.peer.PeerComponent;
import swa.swazam.peer.PeerController;
import swa.swazam.peer.preprocessmusic.MusicManager;
import swa.swazam.util.dto.RequestDTO;

public class RequestManager implements PeerComponent {

	private PeerController controller;

	@Override
	public void setup(PeerController controller) {
		this.controller = controller;
	}

	@Override
	public void destroy() {
		// for now: nop by design
	}

	public void process(RequestDTO request) {
		long timeStamp = System.currentTimeMillis();

		MusicManager mm = controller.getMusicManager();
		File file = mm.match(request.getFingerprint());
		if (file == null) {
			if (request.getTtl() == 0 || request.getTimer() < (short) (System.currentTimeMillis() - timeStamp)) return;
			request.setTtl((short) (request.getTtl() - 1));
			request.setTimer(request.getTimer() - (short) (System.currentTimeMillis() - timeStamp));
			controller.forwardRequest(request);
		} else {
			controller.solveRequest(request, mm.getArtist(file), mm.getTitle(file));
		}
	}
}
