package swa.swazam.util.communication.api.intern.stub;

import java.net.InetSocketAddress;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import swa.swazam.util.communication.General2Peer;
import swa.swazam.util.communication.api.intern.ClientSide;
import swa.swazam.util.communication.api.intern.dto.NetPacketFactory;
import swa.swazam.util.communication.api.intern.dto.RequestWirePacket;
import swa.swazam.util.dto.RequestDTO;

public class General2PeerStub implements General2Peer {
	protected final ClientSide clientSide;

	public General2PeerStub(ClientSide clientSide) {
		this.clientSide = clientSide;
	}

	@Override
	public void process(RequestDTO request, List<InetSocketAddress> destinationPeers) {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("solved", request);

		for (InetSocketAddress destinationPeer : destinationPeers) {
			ChannelFuture connectFuture = clientSide.connect(destinationPeer);
			Channel channel = connectFuture.awaitUninterruptibly().getChannel();
			if (!connectFuture.isSuccess()) {
				continue;
			}
			clientSide.callRemoteMethodNoneBlocking(channel, packet).awaitUninterruptibly();

			channel.close();
		}
	}
}
