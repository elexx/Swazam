package swa.swazam.util.communication.api.intern.stub;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import swa.swazam.util.communication.General2Server;
import swa.swazam.util.communication.api.Startable;
import swa.swazam.util.communication.api.intern.ClientSide;
import swa.swazam.util.communication.api.intern.dto.NetPacketFactory;
import swa.swazam.util.communication.api.intern.dto.RequestWirePacket;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.exceptions.CommunicationException;
import swa.swazam.util.exceptions.SwazamException;

public class Peer2ServerStub implements General2Server, Startable {

	private final ClientSide clientSide;
	private final SocketAddress localListenAddress;
	private Channel channel;

	public Peer2ServerStub(ClientSide clientSide, SocketAddress localListenAddress) {
		this.clientSide = clientSide;
		this.localListenAddress = localListenAddress;
	}

	@Override
	public void startup() throws SwazamException {
		try {
			ChannelFuture connectFuture = clientSide.connect(new InetSocketAddress(InetAddress.getByName("localhost"), 9090));
			channel = connectFuture.awaitUninterruptibly().getChannel();
			if (!connectFuture.isSuccess()) {
				throw new CommunicationException("connect not successful", connectFuture.getCause());
			}
		} catch (UnknownHostException e) {
			throw new CommunicationException("unknown host", e.getCause());
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
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("getPeerList", localListenAddress);
		return clientSide.callRemoteMethode(channel, packet);
	}
}
