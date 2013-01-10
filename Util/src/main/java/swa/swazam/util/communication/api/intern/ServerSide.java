package swa.swazam.util.communication.api.intern;

import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import swa.swazam.util.communication.api.Startable;
import swa.swazam.util.communication.api.intern.dto.NetPacketFactory;
import swa.swazam.util.communication.api.intern.dto.RequestWirePacket;
import swa.swazam.util.communication.api.intern.dto.ResponseWirePacket;
import swa.swazam.util.communication.api.intern.net.PipelineFactoryFactory;
import swa.swazam.util.exceptions.SwazamException;

public class ServerSide extends SimpleChannelUpstreamHandler implements Startable {

	private final SocketAddress localAddress;
	private final ServerBootstrap bootstrap;
	private Object callback;
	private Map<String, Method> callbackMethods;
	private final ChannelGroup allChannels;

	public ServerSide(SocketAddress localAddress) {
		this.localAddress = localAddress;

		allChannels = new DefaultChannelGroup("server");

		bootstrap = new ServerBootstrap();
		bootstrap.setFactory(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(PipelineFactoryFactory.createFactoryWithHandler(this));
	}

	public void setCallback(Object callback) {
		this.callback = callback;

		Method[] methods = callback.getClass().getMethods();
		callbackMethods = new HashMap<String, Method>(methods.length);
		for (Method method : methods) {
			callbackMethods.put(method.getName() + method.getParameterTypes().length, method);
		}
	}

	@Override
	public void startup() throws SwazamException {
		Channel serverChannel = bootstrap.bind(localAddress);
		allChannels.add(serverChannel);
	}

	@Override
	public void shutdown() {
		allChannels.close().awaitUninterruptibly(1, TimeUnit.SECONDS);
		bootstrap.shutdown();
		bootstrap.releaseExternalResources();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		RequestWirePacket requestPacket = (RequestWirePacket) e.getMessage();

		Object[] methodParameters = requestPacket.getParameterList();
		String methodIdentifier = requestPacket.getMethodName() + methodParameters.length;

		Object returnValue;

		if (callbackMethods.containsKey(methodIdentifier)) {
			Method method = callbackMethods.get(methodIdentifier);
			returnValue = method.invoke(callback, methodParameters);
		} else if (methodIdentifier.equals("reportSenderAddress0")) {
			returnValue = ctx.getChannel().getRemoteAddress();
		} else {
			throw new Exception(e.getRemoteAddress() + " tried unkonwn method [" + methodIdentifier + "]");
		}

		ResponseWirePacket responsePacket = NetPacketFactory.createResponseWirePacket(requestPacket, returnValue);

		e.getChannel().write(responsePacket);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		super.exceptionCaught(ctx, e);
	}
}
