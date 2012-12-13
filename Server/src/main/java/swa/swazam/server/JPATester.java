package swa.swazam.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import swa.swazam.server.entity.User;
import swa.swazam.server.service.UserService;

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
		
		 ApplicationContext context = 
                 new ClassPathXmlApplicationContext("applicationContext.xml");
		 
		 UserService us = (UserService) context.getBean("userService");
		 User user = new User("test", "12345", "Christina", "Zrelski", "christina.zrelski@gmail.com", 100);
		 us.save(user);
	}

}
