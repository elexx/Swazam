package swa.swazam.util.communication.api.intern.stub;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

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

public class Client2ServerStub implements Client2Server, Startable {
	private final ClientSide clientSide;
	private final InetSocketAddress serverAddress;
	private Channel channel;

	public Client2ServerStub(ClientSide clientSide, InetSocketAddress serverAddress) {
		this.clientSide = clientSide;
		this.serverAddress = serverAddress;
	}

	@Override
	public void startup() throws SwazamException {
		ChannelFuture connectFuture = clientSide.connect(serverAddress);
		channel = connectFuture.awaitUninterruptibly().getChannel();
		if (!connectFuture.isSuccess()) {
			throw new CommunicationException("connect not successful", connectFuture.getCause());
		}
	}

	@Override
	public void shutdown() {
		channel.close().awaitUninterruptibly();
	}

	@Override
	public boolean verifyCredentials(CredentialsDTO user) throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("verifyCredentials", user);
		return clientSide.callRemoteMethode(channel, packet);
	}

	@Override
	public List<InetSocketAddress> getPeerList() throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("getPeerList");
		return clientSide.callRemoteMethode(channel, packet);
	}

	@Override
	public void logRequest(CredentialsDTO user, MessageDTO message) throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("logRequest", user, message);
		clientSide.callRemoteMethodNoneBlocking(channel, packet).awaitUninterruptibly();
	}

	@Override
	public boolean hasCoins(CredentialsDTO user) throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("hasCoins", user);
		return clientSide.callRemoteMethode(channel, packet);
	}

	public InetAddress reportSendingAddress() throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("reportSenderAddress");
		InetSocketAddress address = clientSide.callRemoteMethode(channel, packet);
		return address.getAddress();
	}
}
