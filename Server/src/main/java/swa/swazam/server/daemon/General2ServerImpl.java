package swa.swazam.server.daemon;

import java.net.InetSocketAddress;
import java.util.List;

import swa.swazam.util.communication.General2Server;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.exceptions.SwazamException;

public class General2ServerImpl implements General2Server {

	@Override
	public List<InetSocketAddress> getPeerList() throws SwazamException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyCredentials(CredentialsDTO user) throws SwazamException {
		// TODO Auto-generated method stub
		return false;
	}

}
