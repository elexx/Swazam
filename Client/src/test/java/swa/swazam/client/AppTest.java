package swa.swazam.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Arrays;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import ac.at.tuwien.infosys.swa.audio.Fingerprint;

import swa.swazam.util.communication.Client2Server;
import swa.swazam.util.communication.ClientCallback;
import swa.swazam.util.communication.General2Peer;
import swa.swazam.util.communication.api.ClientCommunicationUtil;
import swa.swazam.util.communication.api.CommunicationUtilFactory;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.exceptions.SwazamException;
import swa.swazam.util.peerlist.ArrayPeerList;
import swa.swazam.util.peerlist.PeerList;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CommunicationUtilFactory.class)
public class AppTest {

	private App app;
	@Mock
	private ClientCommunicationUtil commLayer;
	@Mock
	private ClientCallback clientCallback;
	@Mock
	private ClientCallback clientCallback2;
	@Mock
	private General2Peer peerStub;
	@Mock
	private Client2Server serverStub;

	@Before
	public void setUp() throws Exception {
		this.app = new App(0);
		MockitoAnnotations.initMocks(this);

		Mockito.when(commLayer.getPeerStub()).thenReturn(peerStub);
		Mockito.when(commLayer.getServerStub()).thenReturn(serverStub);
	}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testLoadConfig() throws IOException {
		app.loadConfig();
		assertEquals(9091, app.getClientPort());
		assertEquals("localhost", app.getServerAddress().getHostString());
		assertEquals(9090, app.getServerAddress().getPort());
		assertEquals("/target/classes/", app.getSnippetRootDirectory());
		assertEquals("demo.wav", app.getSnippetFileName());
		assertEquals("/target/classes/", app.getPeerListStoragePath());
	}

	@Test
	public void testCommLayer() throws SwazamException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		InetSocketAddress serverAddress = new InetSocketAddress("localhost", 9090);

		PowerMock.mockStatic(CommunicationUtilFactory.class);
		EasyMock.expect(CommunicationUtilFactory.createClientCommunicationUtil(serverAddress)).andReturn(commLayer);
		PowerMock.replay(CommunicationUtilFactory.class);

		setField(app, "serverAddress", serverAddress);
		setField(app, "clientCallback", clientCallback);

		app.setupCommLayer();

		Mockito.verify(commLayer, Mockito.times(1)).setCallback(clientCallback);
		Mockito.verify(commLayer, Mockito.never()).setCallback(clientCallback2);
		Mockito.verify(commLayer, Mockito.times(1)).startup();
		Mockito.verify(commLayer, Mockito.never()).shutdown();

		Assert.assertNotNull(getField(app, "peerStub"));
		Assert.assertNotNull(getField(app, "serverStub"));
		assertEquals(peerStub, getField(app, "peerStub"));
		assertEquals(serverStub, getField(app, "serverStub"));
	}

	private void setField(Object instance, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(instance, value);
	}

	private Object getField(Object instance, String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(instance);
	}

	@Test
	public void loginTest() throws Exception {
		String username = "demo";
		String password = "demo";

		setField(app, "serverStub", serverStub);
		ArgumentCaptor<CredentialsDTO> captor = ArgumentCaptor.forClass(CredentialsDTO.class);
		Mockito.when(serverStub.verifyCredentials(captor.capture())).thenReturn(true);

		assertEquals(true, Whitebox.invokeMethod(app, "login", username, password));
		CredentialsDTO user = captor.getValue();
		assertEquals(user, getField(app, "user"));
		assertEquals("demo", user.getUsername());
	}

	@Test
	public void callCheckForCoinsTest() throws Exception {
		CredentialsDTO user = new CredentialsDTO("demo", "demo");

		setField(app, "serverStub", serverStub);
		setField(app, "user", user);

		Mockito.when(serverStub.hasCoins(user)).thenReturn(true);
		assertEquals(true, Whitebox.invokeMethod(app, "checkforCoins"));
	}

	@Test
	public void initialPeerListMoreThanMinimumPeers() throws Exception {
		PeerList<InetSocketAddress> peerList = new ArrayPeerList<>();

		InetSocketAddress peer1 = Mockito.mock(InetSocketAddress.class);
		InetSocketAddress peer2 = Mockito.mock(InetSocketAddress.class);
		InetSocketAddress peer3 = Mockito.mock(InetSocketAddress.class);
		InetSocketAddress peer4 = Mockito.mock(InetSocketAddress.class);
		InetSocketAddress peer5 = Mockito.mock(InetSocketAddress.class);
		peerList.addAll(Arrays.asList(peer1, peer2, peer3, peer4, peer5));
		setField(app, "peerList", peerList);

		Whitebox.invokeMethod(app, "checkAndUpdateInitialPeerListToMinumumSize");

		assertEquals(5, peerList.size());
		assertEquals(peer1, peerList.getTop(1).get(0));
		assertEquals(peer2, peerList.get(1));
		assertEquals(peer3, peerList.get(2));
		assertEquals(peer4, peerList.get(3));
		assertEquals(peer5, peerList.get(4));

	}

	@Test
	public void mp3ReadCheck() throws Exception {

		String testfile1 = System.getProperty("user.dir") + "/target/classes/demo.wav";

		Fingerprint fingerprint = Whitebox.invokeMethod(app, "readFileAsFingerprint", testfile1);

		Assert.assertNotNull(fingerprint);
	}

}
