package swa.swazam.util.communication.api.intern.stub;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import swa.swazam.util.communication.Peer2Client;
import swa.swazam.util.communication.api.Startable;
import swa.swazam.util.communication.api.intern.ClientSide;
import swa.swazam.util.communication.api.intern.dto.NetPacketFactory;
import swa.swazam.util.communication.api.intern.dto.RequestWirePacket;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.SwazamException;

public class Peer2ClientStub implements Peer2Client, Startable {
	private final ClientSide clientSide;

	public Peer2ClientStub(ClientSide clientSide) {
		this.clientSide = clientSide;
	}

	@Override
	public void startup() throws SwazamException {}

	@Override
	public void shutdown() {}

	@Override
	public void solved(MessageDTO answer, InetSocketAddress client) {
		ChannelFuture connectFuture = clientSide.connect(client);
		Channel channel = connectFuture.awaitUninterruptibly().getChannel();
		if (!connectFuture.isSuccess()) {
			return; // TODO: was soll hier wirklich passieren?! throw new CommunicationException("connect not successful", connectFuture.getCause());
		}

		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("solved", answer);
		clientSide.callRemoteMethode(channel, packet);

		channel.close();
	}
}
