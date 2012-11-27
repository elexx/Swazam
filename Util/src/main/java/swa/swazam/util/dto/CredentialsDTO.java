package swa.swazam.util.dto;

import java.io.Serializable;

public class CredentialsDTO implements Serializable {
	private static final long serialVersionUID = -5353541890287433466L;
	
	private String username;
	private String password; //encrypted

	public CredentialsDTO(String username, String password) {
		this.username = username;
		this.password = password;
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

	@Override
	public String toString() {
		return "CredentialsDTO [username=" + username + ", password="
				+ password + "]";
	}
}
