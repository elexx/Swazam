package swa.swazam.server.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import swa.swazam.server.entity.Request;
import swa.swazam.server.service.HistoryService;

@Component("historyBean")
@Scope(value="session")
public class HistoryBean implements Serializable{
	private static final long serialVersionUID = -7592726406181726312L;

	@Autowired
	private HistoryService historyService;
	
	private List<Request> requestsFromUser;
	private List<Request> solvedFromUser;
	private String loggedInUser;
	
	public HistoryBean(){
		requestsFromUser = new ArrayList<Request>(); 
		solvedFromUser = new ArrayList<Request>(); 
		loggedInUser = ((LoginBean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loginBean")).getUsername();
	}
	
	@PostConstruct
	public void init(){
		refreshTables();
	}
	
	/**
	 * Reloads the table data
	 */
	public void refreshTables(){
		requestsFromUser = historyService.getAllRequestedRequestsFromUser(loggedInUser);
		solvedFromUser = historyService.getAllSolvedRequestsFromUser(loggedInUser);
		
		Collections.sort(requestsFromUser, new RequestComparator());
		Collections.sort(solvedFromUser, new RequestComparator());
	}

	public List<Request> getRequestsFromUser() {
		return requestsFromUser;
	}

	public void setRequestsFromUser(List<Request> requestsFromUser) {
		this.requestsFromUser = requestsFromUser;
	}
	
	public List<Request> getSolvedFromUser() {
		return solvedFromUser;
	}

	public void setSolvedFromUser(List<Request> solvedFromUser) {
		this.solvedFromUser = solvedFromUser;
	}

	public String getLoggedInUser() {
		return loggedInUser;
	}

}
