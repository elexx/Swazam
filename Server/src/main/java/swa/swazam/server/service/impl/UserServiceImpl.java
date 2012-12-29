package swa.swazam.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swa.swazam.server.dao.UserDao;
import swa.swazam.server.entity.User;
import swa.swazam.server.service.UserService;
import swa.swazam.util.hash.HashGenerator;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
	private UserDao userDao;
	
	public boolean save(User u){
		return userDao.save(u);
	}

	@Override
	public boolean update(User u) {
		return userDao.update(u);
	}

	@Override
	public boolean delete(String username) {
		return userDao.delete(username);		
	}

	@Override
	public User find(String username) {
		return userDao.find(username);
	}

	@Override
	public User login(String username, String password) {
		User found = find(username);
		if(found == null || HashGenerator.checkPassword(password, found.getPassword()) == false || found.getActive() == false)
			return null;
		
		return found;
	}
}
