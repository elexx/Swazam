package swa.swazam.server.bean;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import swa.swazam.server.entity.User;
import swa.swazam.server.service.UserService;

@Component("loginBean")
@Scope(value="session")
public class LoginBean implements Serializable{
	private static final long serialVersionUID = 6265719303860566896L;
	
	private String username;
	private String password; 
	private User loggedInUser;
	private boolean isLoggedIn = false;
	
	@Autowired
	private UserService userService;
	
	public LoginBean(){}
	
	/**
	 * Processes the login of a certain user
	 * when given a username and a password
	 * @return the start page after login or stays on page if login was unsuccessful
	 */
	public String login(){
		User found = userService.login(username, password);
		if(found != null){
			loggedInUser = found;
			isLoggedIn = true;
			return "History.xhtml?faces-redirect=true";
		}
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Username or password wrong. Please try again", ""));
		return "";
	}
	
	/**
	 * Processes the logout request and destroys the active session
	 * @return the login page
	 */
	public String logout(){
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		username = null;
		password = null;
		loggedInUser = null;
		isLoggedIn = false;
		
		return "Login.xhtml?faces-redirect=true";
	}
	
	/**
	 * Processes the delete request of the current logged in user
	 * @return the login page after deletion was successful
	 */
	public String deleteAccount(){
		loggedInUser.setActive(false);
		boolean success = userService.update(loggedInUser);
		if(success)
			return logout();
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Deletion not successful. Please try again.", ""));
		return "";
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public User getLoggedInUser(){
		return loggedInUser;
	}
	
	public boolean getIsLoggedIn(){
		return isLoggedIn;
	}
}
