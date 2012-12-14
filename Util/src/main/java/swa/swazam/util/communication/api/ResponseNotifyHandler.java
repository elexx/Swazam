package swa.swazam.util.communication.api;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import swa.swazam.util.communication.api.intern.dto.ResponseWirePacket;

class ResponseNotifyHandler extends SimpleChannelUpstreamHandler {
	private final Notifiable notifiable;

	public ResponseNotifyHandler(Notifiable notifiable) {
		this.notifiable = notifiable;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		ResponseWirePacket p = (ResponseWirePacket) e.getMessage();
		notifiable.notifyId(p.getId(), p);
	}
}
