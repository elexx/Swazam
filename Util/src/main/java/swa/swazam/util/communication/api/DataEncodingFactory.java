package swa.swazam.util.communication.api;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

class DataEncodingFactory {
	public static ChannelHandler createEncoder() {
		return new ObjectEncoder();
	}

	public static ChannelHandler createDecoder() {
		return new ObjectDecoder(ClassResolvers.cacheDisabled(DataEncodingFactory.class.getClassLoader()));
	}
}
