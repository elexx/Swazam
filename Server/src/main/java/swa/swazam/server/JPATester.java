package swa.swazam.server;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import swa.swazam.server.entity.User;

public class JPATester {
	public static void main(String[] args){
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("swazam");
		EntityManager em = emf.createEntityManager();
		
		User user = new User("christina", "12345", "Christina", "Zrelski", "christina.zrelski@gmail.com", 100);
		
		em.getTransaction().begin();
		em.persist(user);
		em.getTransaction().commit();
	}

}
