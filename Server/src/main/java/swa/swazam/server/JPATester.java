package swa.swazam.server;

import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import swa.swazam.server.entity.Request;
import swa.swazam.server.entity.User;

public class JPATester {
	public static void main(String[] args){
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("swazam");
		EntityManager em = emf.createEntityManager();
		
		User user = new User("christina", "12345", "Christina", "Zrelski", "christina.zrelski@gmail.com", 100);
				
		em.getTransaction().begin();
		//em.persist(user);
		User u = em.find(User.class, "christina");
		
		Request request = new Request("Bards song", "Blind guardian", new Date(System.currentTimeMillis()), u, null, false, UUID.randomUUID());
		em.persist(request);
		em.getTransaction().commit();
	}

}
