package swa.swazam.server;

import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import swa.swazam.server.entity.Request;
import swa.swazam.server.entity.User;
import swa.swazam.server.service.HistoryService;
import swa.swazam.server.service.impl.HistoryServiceImpl;
import swa.swazam.server.service.impl.UserServiceImpl;

public class JPATester {
	public static void main(String[] args){
		/*EntityManagerFactory emf = Persistence.createEntityManagerFactory("swazam");
		EntityManager em = emf.createEntityManager();
		
		User user = new User("christina", "12345", "Christina", "Zrelski", "christina.zrelski@gmail.com", 100);
				
		em.getTransaction().begin();
		//em.persist(user);
		User u = em.find(User.class, "christina");
		
		Request request = new Request("Bards song", "Blind guardian", new Date(System.currentTimeMillis()), u, null, false, UUID.randomUUID());
		em.persist(request);
		em.getTransaction().commit();*/
		
		/* ApplicationContext context = 
                 new ClassPathXmlApplicationContext("applicationContext.xml");*/
		 
		/* UserServiceImpl us = (UserServiceImpl) context.getBean("userServiceImpl");
		 User user = new User("christina", "12345", "Christina", "Zrelski", "christina.zrelski@gmail.com", 100);
		 us.save(user);*/
		 
		 //HistoryServiceImpl hs = (HistoryServiceImpl) context.getBean("historyServiceImpl");
		 //hs.getAllRequestedRequestsFromUser("chrissi");
		 

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("swazam");
			EntityManager em = emf.createEntityManager();
			
			//User user = new User("christina", "12345", "Christina", "Zrelski", "christina.zrelski@gmail.com", 100);
					
			em.getTransaction().begin();
			//em.persist(user);
			User req = em.find(User.class, "chrissi");
			User solv = em.find(User.class, "markus");
			
			Request request = new Request("She doesnt mind", "Sean Paul", new Date(System.currentTimeMillis()-90000000L), solv, req, true, UUID.randomUUID());
			em.persist(request);
			em.getTransaction().commit();
	}

}
