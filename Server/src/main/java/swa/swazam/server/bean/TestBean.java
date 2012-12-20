package swa.swazam.server.bean;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import swa.swazam.server.entity.User;
import swa.swazam.server.service.impl.UserServiceImpl;

@Component("TestBean")
@Scope(value="session")
public class TestBean implements Serializable {
	private static final long serialVersionUID = 7210211154402593080L;
	
	@Autowired
	private UserServiceImpl userService;

	private String test = "Hello World";

	public TestBean() {
	}
	
	@PostConstruct
	public void print(){
		System.out.println("[TestBean] injected userService " + userService);
	}

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}
	
	public void click(){
		User u = new User("1234", "mypass", "hugo", "hugo", "hugo@hugo.hugo", 5);
		userService.save(u);
	}
}
