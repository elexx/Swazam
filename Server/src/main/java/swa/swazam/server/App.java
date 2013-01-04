package swa.swazam.server;

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

    public void startWebThread(){
	webRunnable = new WebThread();
	webThread = new Thread(webRunnable, "WebThread");
	webThread.run();
    }

    public void startDaemonThread(){
	daemonThread = new Thread(new Runnable() {

	    @Override
	    public void run() {
		try {
		    serverCallback.setPeerList(backup.loadPeers());
		    commLayer = CommunicationUtilFactory.createServerCommunicationUtil();
		    commLayer.setCallback(serverCallback);
		    commLayer.startup();
		} catch (SwazamException e) {
		}
	    }
	});

	daemonThread.run();
    }
    
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
    }

    public void read(){

	Scanner sc =  new Scanner(System.in);
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