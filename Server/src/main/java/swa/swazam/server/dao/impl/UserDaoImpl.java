package swa.swazam.server.dao.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import swa.swazam.server.dao.UserDao;
import swa.swazam.server.entity.User;

@Repository
public class UserDaoImpl implements UserDao {
	
	private EntityManager entityManager;
	
	@PersistenceContext
	public void setEntityManager(EntityManager entityManager){
		this.entityManager = entityManager;
	}
	
	@Override
	@Transactional
	public boolean save(User u){
		User alreadyExists = this.find(u.getUsername());
		if(alreadyExists != null)
			return false;
		
		entityManager.persist(u);
		return true;
	}
	
	@Override
	@Transactional
	public boolean update(User u){
		User alreadyExists = this.find(u.getUsername());
		if(alreadyExists == null)
			return false;
		
		entityManager.merge(u);
		return true;
	}
	
	@Override
	@Transactional
	public boolean delete(String username){
		User alreadyExists = this.find(username);
		if(alreadyExists == null)
			return false;
		
		entityManager.remove(username);
		return true;
	}
	
	@Override
	@Transactional
	public User find(String username){
		return entityManager.find(User.class, username);
	}
}
