package swa.swazam.util.communication.api.intern.stub;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import swa.swazam.util.communication.Client2Server;
import swa.swazam.util.communication.api.Startable;
import swa.swazam.util.communication.api.intern.ClientSide;
import swa.swazam.util.communication.api.intern.dto.NetPacketFactory;
import swa.swazam.util.communication.api.intern.dto.RequestWirePacket;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.CommunicationException;
import swa.swazam.util.exceptions.SwazamException;

public class Client2ServerStub extends ClientSide implements Client2Server, Startable {

	private final ClientBootstrap bootstrap;
	private Channel channel;

	public Client2ServerStub(ClientBootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	@Override
	public void startup() throws SwazamException {
		try {
			ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(InetAddress.getByName("localhost"), 9090));
			channel = connectFuture.awaitUninterruptibly().getChannel();
			if (!connectFuture.isSuccess()) {
				bootstrap.releaseExternalResources();
				throw new CommunicationException("connect not successful", connectFuture.getCause());
			}
		} catch (UnknownHostException e) {
			throw new CommunicationException("unknown host", e.getCause());
		}
	}

	@Override
	public void shutdown() {
		channel.close().awaitUninterruptibly();
		bootstrap.releaseExternalResources();
	}

	@Override
	public boolean verifyCredentials(CredentialsDTO user) throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("verifyCredentials", user);
		return callRemoteMethode(channel, packet);
	}

	@Override
	public List<InetSocketAddress> getPeerList() throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("getPeerList");
		return callRemoteMethode(channel, packet);
	}

	@Override
	public void logRequest(CredentialsDTO user, MessageDTO message) throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("logRequest", user, message);
		callRemoteMethode(channel, packet);
	}

	@Override
	public boolean hasCoins(CredentialsDTO user) throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("hasCoins", user);
		return callRemoteMethode(channel, packet);
	}
}
