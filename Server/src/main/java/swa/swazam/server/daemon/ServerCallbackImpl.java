package swa.swazam.server.daemon;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import swa.swazam.server.entity.Request;
import swa.swazam.server.entity.User;
import swa.swazam.server.service.HistoryService;
import swa.swazam.server.service.UserService;
import swa.swazam.util.communication.ServerCallback;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.SwazamException;
import swa.swazam.util.peerlist.ArrayPeerList;

@Component
public class ServerCallbackImpl implements ServerCallback {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private HistoryService historyService;
	
	private ArrayPeerList<InetSocketAddress> peerList;
	
	public ServerCallbackImpl(){
		peerList = new ArrayPeerList<InetSocketAddress>();
	}

	@Override
	public List<InetSocketAddress> getPeerList() throws SwazamException {
		return peerList.getTop(5);
	}

	@Override
	public List<InetSocketAddress> getPeerList(InetSocketAddress sender) throws SwazamException {
		List<InetSocketAddress> top5 = peerList.getTop(5);
		peerList.push(sender);
		return top5;
	}

	@Override
	public boolean hasCoins(CredentialsDTO user) throws SwazamException {
		if(!verifyCredentials(user))
			throw new SwazamException("The given credentials are wrong or do not exist.");
			
		return userService.hasCoins(user.getUsername());
	}

	@Override
	public void logRequest(CredentialsDTO user, MessageDTO message) throws SwazamException {
		if(!verifyCredentials(user)) 
			throw new SwazamException("The given credentials are wrong or do not exist.");
		
		User requestor = userService.find(user.getUsername());
		User solver = null;
		if(message.getResolver() != null){
			solver = userService.find(message.getResolver().getUsername());
			solver.setCoins(solver.getCoins()+2);
			userService.update(solver);
		}
		else {
			requestor.setCoins(requestor.getCoins()-1);
			userService.update(requestor);
		}
		
		Request r = new Request(message.getSongTitle(), message.getSongArtist(), new Date(System.currentTimeMillis()), requestor, solver, false, message.getUuid());
		historyService.saveOrUpdateRequest(r);
		
		//TODO add solver InetSocketAddress to peer List
	}

	@Override
	public boolean verifyCredentials(CredentialsDTO user) throws SwazamException {
		User fullUserObject = userService.find(user.getUsername());
		if(fullUserObject == null)
			return false;
		
		return user.getPassword().equals(fullUserObject.getPassword());
	}

}
