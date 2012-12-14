package swa.swazam.util.communication.api.intern.net;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;

public class PipelineFactoryFactory {
	public static ChannelPipelineFactory createFactoryWithHandler(final ChannelUpstreamHandler handler) {
		return new ChannelPipelineFactory() {
			private final ChannelHandler encoder = DataEncodingFactory.createEncoder();
			private final ChannelHandler decoder = DataEncodingFactory.createDecoder();

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();

				pipeline.addLast("encoder", encoder);
				pipeline.addLast("decoder", decoder);
				pipeline.addLast("handler", handler);

				return pipeline;
			}
		};
	}
}
