package swa.swazam.server.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import swa.swazam.server.dao.UserDao;
import swa.swazam.server.entity.User;

@Service
public class UserService {

	@Autowired
	private UserDao userDao;
	
	@PostConstruct
	public void print(){
		System.out.println("[UserService] injected userDao " + userDao);
	}
	
	public void save(User u){
		userDao.save(u);
	}
}
