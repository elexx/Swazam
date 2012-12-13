package swa.swazam.util.communication.api;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import swa.swazam.util.communication.ServerCallback;
import swa.swazam.util.exceptions.SwazamException;

class ServerCommunicationUtilImpl extends SimpleChannelUpstreamHandler implements ServerCommunicationUtil {

	private ServerCallback callback;
	private Map<String, Method> callbackMethods;
	private ServerBootstrap sBootstrap;

	@Override
	public void setCallback(ServerCallback callback) {
		this.callback = callback;

		Method[] methods = callback.getClass().getMethods();
		callbackMethods = new HashMap<String, Method>(methods.length);
		for (Method method : methods) {
			callbackMethods.put(method.getName(), method);
		}
	}

	@Override
	public void startup() throws SwazamException {

		sBootstrap = new ServerBootstrap();
		sBootstrap.setFactory(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		sBootstrap.setPipelineFactory(PipelineFactoryFactory.createFactoryWithHandler(this));

		sBootstrap.bind(new InetSocketAddress(9090));
	}

	@Override
	public void shutdown() {
		sBootstrap.releaseExternalResources();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		RequestWirePacket p = (RequestWirePacket) e.getMessage();

		if (!callbackMethods.containsKey(p.getMethodName())) {
			throw new Exception(e.getRemoteAddress() + " tried unkonwn method [" + p.getMethodName() + "]");
		}

		Method method = callbackMethods.get(p.getMethodName());
		Object returnValue = method.invoke(callback, p.getParameterList());

		ResponseWirePacket responsePacket = NetPacketFactory.createResponseWirePacket(p, returnValue);

		e.getChannel().write(responsePacket);
	}
}
