package swa.swazam.peer;

import java.net.InetSocketAddress;

import swa.swazam.peer.preprocessmusic.MusicManager;
import swa.swazam.util.dto.RequestDTO;
import swa.swazam.util.peerlist.PeerList;

public interface PeerController {

	PeerList<InetSocketAddress> getPeerList();

	MusicManager getMusicManager();

	void solveRequest(RequestDTO request, String artist, String title);

	void forwardRequest(RequestDTO request);

	String getStorageRoot();

}
