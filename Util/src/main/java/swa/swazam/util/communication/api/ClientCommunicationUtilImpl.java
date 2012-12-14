package swa.swazam.util.communication.api;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import swa.swazam.util.communication.Client2Server;
import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.communication.api.intern.net.PipelineFactoryFactory;
import swa.swazam.util.exceptions.CommunicationException;
import swa.swazam.util.exceptions.SwazamException;

class ClientCommunicationUtilImpl implements ClientCommunicationUtil, WriteChannel {

	private final Client2ServerStub client2ServerStub;
	private final ClientBootstrap serverConnectionBootstrap;
	private ClientCallback callback;
	private Channel channel;
	private ChannelFuture lastWriteFuture;

	ClientCommunicationUtilImpl() {
		client2ServerStub = new Client2ServerStub(this);
		serverConnectionBootstrap = new ClientBootstrap();
		serverConnectionBootstrap.setFactory(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		serverConnectionBootstrap.setPipelineFactory(PipelineFactoryFactory.createFactoryWithHandler(new ResponseNotifyHandler(client2ServerStub)));
	}

	@Override
	public void startup() throws SwazamException {
		try {
			ChannelFuture future = serverConnectionBootstrap.connect(new InetSocketAddress(InetAddress.getByName("localhost"), 9090));
			channel = future.awaitUninterruptibly().getChannel();
			if (!future.isSuccess()) {
				serverConnectionBootstrap.releaseExternalResources();
				throw new CommunicationException("connect not successful", future.getCause());
			}
		} catch (UnknownHostException e) {
			throw new CommunicationException("unknown host", e.getCause());
		}
	}

	@Override
	public void setCallback(ClientCallback callback) {
		this.callback = callback;
	}

	@Override
	public Client2Server getServerStub() {
		return client2ServerStub;
	}

	@Override
	public void shutdown() {
		waitForChannel();
		channel.close().awaitUninterruptibly();
		serverConnectionBootstrap.releaseExternalResources();
	}

	@Override
	public void write(Object message) {
		waitForChannel();
		lastWriteFuture = channel.write(message);
	}

	private void waitForChannel() {
		if (lastWriteFuture != null) {
			lastWriteFuture.awaitUninterruptibly();
		}
	}
}
