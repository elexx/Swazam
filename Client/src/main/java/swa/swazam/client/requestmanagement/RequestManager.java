package swa.swazam.client.requestmanagement;

import swa.swazam.client.ClientApp;
import swa.swazam.util.dto.RequestDTO;

public class RequestManager {
	
	private ClientApp app;

	public void setup(ClientApp app) {	
	 	this.app = app;
	}
	
	public void destroy() {
	
	}
	
	public void sendRequests(RequestDTO request) {
		
	}
}
