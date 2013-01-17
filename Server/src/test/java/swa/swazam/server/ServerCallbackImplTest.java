package swa.swazam.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import swa.swazam.server.daemon.ServerCallbackImpl;
import swa.swazam.server.entity.User;
import swa.swazam.server.service.HistoryService;
import swa.swazam.server.service.UserService;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.SwazamException;
import swa.swazam.util.hash.HashGenerator;

@RunWith(MockitoJUnitRunner.class)
public class ServerCallbackImplTest {
	
	@InjectMocks
	private ServerCallbackImpl sci = new ServerCallbackImpl();
	
	@Mock
	private UserService userService;
	
	@Mock
	private HistoryService historyService;
	
	private CredentialsDTO user;
	private User fullUser;
	private MessageDTO request;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);

		user = new CredentialsDTO("chrissi", HashGenerator.hash("chrissi"));
		fullUser = new User("chrissi", HashGenerator.hash("chrissi"), "christina", "zrelski", "christina@zrelski.com", 100, true);
		request = new MessageDTO(UUID.randomUUID(), null, null, null);
	}

	@Test
	public void test_verifyCredentialsShouldReturnTrue() {
		when(userService.find(user.getUsername())).thenReturn(fullUser);
		
		try {
			boolean verify = sci.verifyCredentials(user);
			assertTrue(verify);
		} catch (SwazamException e) {
			fail();
		}
	}
	
	@Test
	public void test_verifyCredentialsShouldReturnFalse() {
		when(userService.find("notExistingUser")).thenReturn(fullUser);
		
		try {
			boolean verify = sci.verifyCredentials(user);
			assertFalse(verify);
		} catch (SwazamException e) {
			fail();
		}
	}
	
	@Test
	public void test_hasCoinsShouldReturnTrue(){
		when(userService.find(user.getUsername())).thenReturn(fullUser);
		when(userService.hasCoins(user.getUsername())).thenReturn(true);
		
		try {
			boolean coinsLeft = sci.hasCoins(user);
			assertTrue(coinsLeft);
		} catch (SwazamException e) {
			fail();
		}
	}
	
	@Test
	public void test_hasCoinsShouldReturnFalse(){
		when(userService.find(user.getUsername())).thenReturn(fullUser);
		when(userService.hasCoins(user.getUsername())).thenReturn(false);
		
		try {
			boolean coinsLeft = sci.hasCoins(user);
			assertFalse(coinsLeft);
		} catch (SwazamException e) {
			fail();
		}
	}	
	
	@Test
	public void test_hasCoinsShouldThrowSwazamException(){
		when(userService.find(user.getUsername())).thenReturn(null);
		when(userService.hasCoins(user.getUsername())).thenReturn(false);
		
		try {
			sci.hasCoins(user);
		} catch (SwazamException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void test_logRequestShouldThrowSwazamException(){
		when(userService.find("bla")).thenReturn(null);
		
		try {
			sci.logRequest(user, request);
			fail();
		} catch (SwazamException e) {
			assertTrue(true);
		}
	}
}
