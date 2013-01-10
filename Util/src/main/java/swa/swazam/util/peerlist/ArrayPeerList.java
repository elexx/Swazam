package swa.swazam.util.peerlist;

import java.util.ArrayList;
import java.util.List;

public class ArrayPeerList<T> extends ArrayList<T> implements PeerList<T> {

	private static final long serialVersionUID = 974679576323988400L;

	@Override
	public void push(T peer) {
		super.remove(peer);
		super.add(0, peer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] getTopArray(int n) {
		return (T[]) getTop(n).toArray();
	}

	@Override
	public List<T> getTop(int n) {
		return new ArrayList<T>(super.subList(0, Math.min(n, super.size())));
	}
}
