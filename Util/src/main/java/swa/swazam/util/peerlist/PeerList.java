package swa.swazam.util.peerlist;

import java.util.List;

/**
 * A generic list with the capability of maintaining a last-seen order. The
 * push(T) method allows for updating the "seen"-status of n object, the
 * getTop(int) and getTopArray(int) methods allow for getting the last n seen
 * objects.
 * 
 * @param <T>
 */
public interface PeerList<T> extends List<T> {
	/**
	 * Set the object as "last-seen". If the object is not in the list, it is
	 * inserted (on top of it). If it already is in the list, the ordering is
	 * updated to reflect tione last-seen status (it is moved to the top).
	 * 
	 * @param peer
	 *            The object to update/insert
	 */
	void push(T peer);

	/**
	 * Returns the top (last-seen) n elements as an array. If the list is
	 * shorter than n, all elements are returned.
	 * 
	 * @param n
	 *            The number of elements to return, containing n elements or
	 *            less
	 * @return The generated array
	 */
	T[] getTopArray(int n);

	/**
	 * Returns the top (last-seen) n elements as a list. If the list is shorter
	 * than n, all elements are returned.
	 * 
	 * @param n
	 *            The number of elements to return, containing n elements or
	 *            less
	 * @return The generated list
	 */
	List<T> getTop(int n);
}
