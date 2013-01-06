package swa.swazam.util.communication;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;

import swa.swazam.util.communication.api.CommunicationUtilFactory;
import swa.swazam.util.communication.api.PeerCommunicationUtil;
import swa.swazam.util.dto.RequestDTO;
import ac.at.tuwien.infosys.swa.audio.Fingerprint;
import ac.at.tuwien.infosys.swa.audio.SubFingerprint;

public class PeerServerSideCommunication {
	private PeerCommunicationUtil commUtil;

	protected RequestDTO request;
	protected InetSocketAddress sender;

	@Before
	public final void startup() throws Exception {
		request = new RequestDTO(UUID.randomUUID(), new InetSocketAddress("localhost", 1234), new Fingerprint(0, 0, Arrays.asList(new SubFingerprint(123), new SubFingerprint(456))));
		sender = new InetSocketAddress(0);

		commUtil = CommunicationUtilFactory.createPeerCommunicationUtil(new InetSocketAddress(9090));
		commUtil.setCallback(mockServer());
		commUtil.startup();
	}

	@After
	public final void shutdown() {
		commUtil.shutdown();
	}

	private PeerCallback mockServer() throws Exception {
		PeerCallback callback = mock(PeerCallback.class);
		doNothing().when(callback).alive(sender);
		doNothing().when(callback).process(request);
		return callback;
	}

	public PeerCommunicationUtil getCommUtil() {
		return commUtil;
	}
}
