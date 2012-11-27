package swa.swazam.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

import swa.swazam.util.MyUtil;

/**
 * Hello world!
 */
public class App {
	public static void main(String[] args) {
		System.out.println("Hello World Server");
		System.out.println(new MyUtil().getIt());
	
		startServer();
	}
	
	private static void startServer(){
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] {createWebAppContext()});
		
		final Server server = new Server(8080);
		server.setHandler(contexts);
		Runnable thread = new Runnable(){

			@Override
			public void run() {
				try {
					server.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		thread.run();
	}
	
	private static WebAppContext createWebAppContext(){
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setDescriptor("/WEB-INF/web.xml");
		webAppContext.setResourceBase(".");
		webAppContext.setContextPath("/");
		
		return webAppContext;
	}
}
