package swa.swazam.server.dao;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import swa.swazam.server.entity.User;

@Repository
public class UserDao {
	
	private EntityManager entityManager;
	
	@PersistenceContext
	public void setEntityManager(EntityManager entityManager){
		this.entityManager = entityManager;
	}
	
	@PostConstruct
	public void print(){
		System.out.println("[UserDao] injected EntityManager " + entityManager);
	}
	
	@Transactional
	public void save(User u){
		entityManager.persist(u);
	}

}
