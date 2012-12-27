package swa.swazam.util.communication.api.intern.stub;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import swa.swazam.util.communication.Peer2Peer;
import swa.swazam.util.communication.api.Startable;
import swa.swazam.util.communication.api.intern.ClientSide;
import swa.swazam.util.communication.api.intern.dto.NetPacketFactory;
import swa.swazam.util.communication.api.intern.dto.RequestWirePacket;
import swa.swazam.util.dto.RequestDTO;
import swa.swazam.util.exceptions.SwazamException;

public class Peer2PeerStub implements Peer2Peer, Startable {
	private final ClientSide clientSide;

	public Peer2PeerStub(ClientSide clientSide) {
		this.clientSide = clientSide;
	}

	@Override
	public void startup() throws SwazamException {}

	@Override
	public void shutdown() {}

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

	@Override
	public List<InetSocketAddress> alive(List<InetSocketAddress> destinationPeers) {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("alive");

		List<ChannelFuture> futures = new LinkedList<>();
		List<InetSocketAddress> answers = new LinkedList<>();

		for (InetSocketAddress destinationPeer : destinationPeers) {
			ChannelFuture connectFuture = clientSide.connect(destinationPeer);
			Channel channel = connectFuture.awaitUninterruptibly().getChannel();
			if (!connectFuture.isSuccess()) {
				continue;
			}
			ChannelFuture future = clientSide.callRemoteMethodNoneBlocking(channel, packet).awaitUninterruptibly();
			futures.add(future);
		}

		long maxWaitingTime = 2000;
		for (ChannelFuture future : futures) {
			if (future.awaitUninterruptibly(maxWaitingTime, TimeUnit.MILLISECONDS)) {
				answers.add((InetSocketAddress) future.getChannel().getRemoteAddress());
			}
			future.cancel();
			future.getChannel().close();
		}

		return answers;
	}
}
