package swa.swazam.server.bean;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import swa.swazam.server.entity.User;
import swa.swazam.server.service.impl.UserServiceImpl;
import swa.swazam.util.hash.HashGenerator;

@Component("registrationBean")
@Scope(value="request")
public class RegistrationBean implements Serializable{

	private static final long serialVersionUID = -3118541481795645972L;
	
	@Autowired
	private UserServiceImpl userService;
	
	private String username;
	private String firstname;
	private String lastname;
	private String password; 
	private String email;
	
	private boolean success = false;
	
	public RegistrationBean() {}
	
	/**
	 * Processes a new registration
	 * In case the username does not already exist the user is registered to the database and a success message is displayed.
	 * Otherwise, a warning is displayed that the username already exists
	 */
	public void register(){
		User user = new User(username, HashGenerator.hash(password), firstname, lastname, email, 100, true);
		success = userService.save(user);
		
		if(success)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Your registration was successful! You can now log in.", ""));
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Username already exists. Please chose a different one", ""));
	}  
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public boolean getSuccess(){
		return !success;
	}
}
