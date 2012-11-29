package swa.swazam.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Hello world!
 */
public class App {
	public static void main(String[] args) {
		String webappDirLocation = "src/main/webapp/";

		Server server = new Server(8080);
		WebAppContext root = new WebAppContext();

		root.setContextPath("/");
		root.setDescriptor(webappDirLocation + "WEB-INF/web.xml");
		root.setResourceBase(webappDirLocation);
		root.setBaseResource(new ResourceCollection(new String[] { webappDirLocation, "target/" }));
		root.setResourceAlias("/WEB-INF/classes/", "/classes/");

		root.setParentLoaderPriority(true);

		server.setHandler(root);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
