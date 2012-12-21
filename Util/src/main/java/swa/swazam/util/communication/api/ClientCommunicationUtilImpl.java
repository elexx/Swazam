package swa.swazam.util.communication.api;

import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import swa.swazam.util.communication.Client2Server;
import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.communication.api.intern.net.PipelineFactoryFactory;
import swa.swazam.util.communication.api.intern.stub.Client2ServerStub;
import swa.swazam.util.exceptions.SwazamException;

class ClientCommunicationUtilImpl implements ClientCommunicationUtil {
	private final Client2ServerStub serverStub;
	private final ClientBootstrap bootstrap;

	ClientCommunicationUtilImpl() {
		bootstrap = new ClientBootstrap();
		bootstrap.setFactory(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

		serverStub = new Client2ServerStub(bootstrap);
		bootstrap.setPipelineFactory(PipelineFactoryFactory.createFactoryWithHandler(serverStub));
	}

	@Override
	public void startup() throws SwazamException {
		serverStub.startup();
	}

	@Override
	public void setCallback(ClientCallback callback) {
		setCallback(callback);
	}

	@Override
	public Client2Server getServerStub() {
		return serverStub;
	}

	@Override
	public void shutdown() {
		serverStub.shutdown();
		bootstrap.releaseExternalResources();
	}
}
