package swa.swazam.server.daemon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import swa.swazam.server.service.UserService;
import swa.swazam.util.communication.Client2Server;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.SwazamException;

@Component
public class Client2ServerImpl extends General2ServerImpl implements Client2Server {
	
	@Autowired
	private UserService userService;
	
	@Override
	public boolean hasCoins(CredentialsDTO user) throws SwazamException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void logRequest(CredentialsDTO user, MessageDTO message) throws SwazamException {
		// TODO Auto-generated method stub
		
	}

}
