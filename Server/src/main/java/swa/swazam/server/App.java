package swa.swazam.server;

import java.net.InetSocketAddress;
import java.util.Scanner;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import swa.swazam.server.daemon.ServerCallbackImpl;
import swa.swazam.util.exceptions.SwazamException;
import swa.swazam.util.peerlist.PeerListBackup;

import swa.swazam.util.communication.api.CommunicationUtilFactory;
import swa.swazam.util.communication.api.ServerCommunicationUtil;

public class App{
    private ServerCallbackImpl serverCallback;
    private PeerListBackup backup;
    private ServerCommunicationUtil commLayer;
    private Thread webThread;
    private Thread daemonThread;
    private Thread backupThread;
    private WebThread webRunnable; 

    public App(){
	ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
	serverCallback = (ServerCallbackImpl) context.getBean("serverCallbackImpl");
	backup = new PeerListBackup("");
	startWebThread();
	startDaemonThread();
	startBackupThread();
	read();
    }

    public static void main(String[] args) {
	App app = new App();
    }

    /**
     * Starts the webserver thread
     */
    public void startWebThread(){
	webRunnable = new WebThread();
	webThread = new Thread(webRunnable, "WebThread");
	webThread.start();
    }

    /**
     * starts the daemon server thread
     */
    public void startDaemonThread(){
	daemonThread = new Thread(new Runnable() {

	    @Override
	    public void run() {
		try {
		    serverCallback.setPeerList(backup.loadPeers());
		    commLayer = CommunicationUtilFactory.createServerCommunicationUtil(new InetSocketAddress(9090));
		    commLayer.setCallback(serverCallback);
		    commLayer.startup();
		} catch (SwazamException e) {
		}
	    }
	});

	daemonThread.start();
    }
    
    /**
     * starts the backup thread for the peer list.
     * Every 15 minutes the peer list is exported to the file system.
     */
    public void startBackupThread(){
	backupThread = new Thread(new Runnable() {
	    
	    @Override
	    public void run() {
		try {
		    while(true){
			Thread.sleep(900000L);
		    	backup.storePeers(serverCallback.getFullPeerList());
		    }
		} catch (SwazamException | InterruptedException e) {
		}
	    }
	});
	
	backupThread.start();
    }

    /**
     * waits on user input for the shutdown of the server
     */
    public void read(){

	Scanner sc =  new Scanner(System.in);
	try {
	    Thread.sleep(10000L);
	} catch (InterruptedException e) {}
	
	System.out.println("\nEnter [quit] to exit");
	while(sc.hasNext()){
	    String command = sc.next();
	    if(command.equals("quit")){
		commLayer.shutdown();
		webRunnable.stop();
		backupThread.interrupt();
		return;
	    }
	}
    }

}