package swa.swazam.server.web;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "TestBean")
@SessionScoped
public class TestBean implements Serializable {

	private String test = "Hello World";

	public TestBean() {

	}

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}
}