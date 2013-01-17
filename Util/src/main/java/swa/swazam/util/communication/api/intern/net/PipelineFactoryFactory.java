package swa.swazam.util.communication.api.intern.net;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;

public class PipelineFactoryFactory {
	public static ChannelPipelineFactory createFactoryWithHandler(final ChannelUpstreamHandler handler) {
		return new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();

				pipeline.addLast("encoder", DataEncodingFactory.createEncoder());
				pipeline.addLast("decoder", DataEncodingFactory.createDecoder());
				pipeline.addLast("handler", handler);

				return pipeline;
			}
		};
	}
}
