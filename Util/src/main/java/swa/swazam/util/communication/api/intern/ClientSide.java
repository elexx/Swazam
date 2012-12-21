package swa.swazam.util.communication.api.intern;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import swa.swazam.util.communication.api.intern.dto.RequestWirePacket;
import swa.swazam.util.communication.api.intern.dto.ResponseWirePacket;

public abstract class ClientSide extends SimpleChannelUpstreamHandler {

	private final Map<Integer, ResponseWirePacket> responses = new HashMap<>();
	private final Lock lock = new ReentrantLock();
	private final Map<Integer, Condition> locks = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	protected <T> T callRemoteMethode(Channel channel, RequestWirePacket packet) {
		Integer id = packet.getId();

		Condition condition = lock.newCondition();
		locks.put(id, condition);
		try {
			lock.lock();
			responses.put(id, null);
			channel.write(packet);
			do {
				condition.awaitUninterruptibly();
			} while (responses.get(id) == null);

		} finally {
			lock.unlock();
		}

		return (T) responses.get(id).getReturnValue();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		ResponseWirePacket responsePacket = (ResponseWirePacket) e.getMessage();
		int id = responsePacket.getId();
		lock.lock();
		try {
			responses.put(id, responsePacket);
			locks.get(id).signal();
		} finally {
			lock.unlock();
		}
	}
}
