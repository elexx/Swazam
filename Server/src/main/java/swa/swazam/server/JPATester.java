package swa.swazam.server;

import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import swa.swazam.server.daemon.ServerCallbackImpl;
import swa.swazam.server.entity.Request;
import swa.swazam.server.entity.User;
import swa.swazam.server.service.HistoryService;
import swa.swazam.server.service.impl.HistoryServiceImpl;
import swa.swazam.server.service.impl.UserServiceImpl;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.SwazamException;
import swa.swazam.util.hash.HashGenerator;

public class JPATester {
	public static void main(String[] args) throws SwazamException{
		/*EntityManagerFactory emf = Persistence.createEntityManagerFactory("swazam");
		EntityManager em = emf.createEntityManager();
		
		User user = new User("christina", "12345", "Christina", "Zrelski", "christina.zrelski@gmail.com", 100);
				
		em.getTransaction().begin();
		//em.persist(user);
		User u = em.find(User.class, "christina");
		
		Request request = new Request("Bards song", "Blind guardian", new Date(System.currentTimeMillis()), u, null, false, UUID.randomUUID());
		em.persist(request);
		em.getTransaction().commit();*/
		
		/*ApplicationContext context = 
                 new ClassPathXmlApplicationContext("applicationContext.xml");
		
		ServerCallbackImpl sci = (ServerCallbackImpl) context.getBean("serverCallbackImpl");
		
		System.out.println(sci);
		CredentialsDTO chrissi = new CredentialsDTO("chrissi", HashGenerator.hash("chrissi"));
		
		MessageDTO message = new MessageDTO(UUID.randomUUID(), null, null, null);
		sci.logRequest(chrissi, message);*/
		
		 
		/* UserServiceImpl us = (UserServiceImpl) context.getBean("userServiceImpl");
		 User user = new User("christina", "12345", "Christina", "Zrelski", "christina.zrelski@gmail.com", 100);
		 us.save(user);*/
		 
		 //HistoryServiceImpl hs = (HistoryServiceImpl) context.getBean("historyServiceImpl");
		 //hs.getAllRequestedRequestsFromUser("chrissi");
		 

		//EntityManagerFactory emf = Persistence.createEntityManagerFactory("swazam");
			//EntityManager em = emf.createEntityManager();
			
			//User user = new User("christina", "12345", "Christina", "Zrelski", "christina.zrelski@gmail.com", 100);
					
			//em.getTransaction().begin();
			//em.persist(user);
			//User req = em.find(User.class, "chrissi");
			//User solv = em.find(User.class, "markus");
			
			//Request request = new Request("fear of the dark", "iron maiden", new Date(System.currentTimeMillis()-10000000L), solv, req, true, UUID.randomUUID());
			//em.persist(request);
			//em.getTransaction().commit();
		
			//	String pw1 = HashGenerator.hash("chrissi");
			//	System.out.println(pw1);
					
		}

}
