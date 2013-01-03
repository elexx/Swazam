package swa.swazam.peer.alive;

import java.net.InetSocketAddress;

import swa.swazam.peer.PeerComponent;
import swa.swazam.peer.PeerController;

public class AliveManager implements PeerComponent {

	private PeerController controller;

	@Override
	public void setup(PeerController controller) {
		this.controller = controller;
	}

	@Override
	public void destroy() {
		// for now: nop by design
	}

	public void alive(InetSocketAddress sender) {
		controller.getPeerList().push(sender);
	}

}
