package swa.swazam.util.communication.api.intern;

import java.net.ConnectException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.timeout.ReadTimeoutException;

import swa.swazam.util.communication.api.Startable;
import swa.swazam.util.communication.api.intern.dto.RequestWirePacket;
import swa.swazam.util.communication.api.intern.dto.ResponseWirePacket;
import swa.swazam.util.communication.api.intern.net.PipelineFactoryFactory;
import swa.swazam.util.exceptions.CommunicationException;
import swa.swazam.util.exceptions.SwazamException;

final public class ClientSide extends SimpleChannelUpstreamHandler implements Startable {

	private final Map<Integer, ResponseWirePacket> responses = new HashMap<>();
	private final Lock lock = new ReentrantLock();
	private final Map<Integer, Condition> locks = new ConcurrentHashMap<>();

	protected final ClientBootstrap bootstrap;

	public ClientSide() {
		bootstrap = new ClientBootstrap();
		bootstrap.setFactory(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setOption("connectTimeoutMillis", 1000);
		bootstrap.setPipelineFactory(PipelineFactoryFactory.createFactoryWithHandler(this));
	}

	public ChannelFuture connect(SocketAddress remoteAddress) {
		return bootstrap.connect(remoteAddress);
	}

	@Override
	public void startup() throws SwazamException {}

	@Override
	public void shutdown() {
		try {
			lock.lock();
			for (Condition condition : locks.values()) {
				condition.signalAll();
			}
		} finally {
			lock.unlock();
		}
		bootstrap.shutdown();
		bootstrap.releaseExternalResources();
	}

	/**
	 * calls a remote method, described by {@link RequestWirePacket}
	 * 
	 * @param channel the channel through which the call should take place
	 * @param packet the describing packet
	 * @return the return value of the remote method
	 * @throws SwazamException
	 */
	@SuppressWarnings("unchecked")
	public <T> T callRemoteMethode(Channel channel, RequestWirePacket packet) throws CommunicationException {
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

		if (!responses.containsKey(id)) {
			throw new CommunicationException("no response available");
		}

		return (T) responses.get(id).getReturnValue();
	}

	/**
	 * similar to {@link #callRemoteMethode(Channel, RequestWirePacket)} this method also invokes a remote method, but it does not block or return any value.<br />
	 * useful for methods witout return value (void)
	 * 
	 * @param channel
	 * @param packet
	 * @return
	 */
	public ChannelFuture callRemoteMethodNoneBlocking(Channel channel, RequestWirePacket packet) {
		return channel.write(packet).awaitUninterruptibly();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		ResponseWirePacket responsePacket = (ResponseWirePacket) e.getMessage();
		int id = responsePacket.getId();
		lock.lock();
		try {
			if (locks.containsKey(id)) {
				responses.put(id, responsePacket);
				locks.get(id).signal();
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if (e.getCause() instanceof ReadTimeoutException) {

		} else if (e.getCause() instanceof ConnectException) {

		} else {
			e.getCause().printStackTrace(System.err);
		}
	}
}
