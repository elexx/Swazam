package swa.swazam.client;

public class TimeLimiter implements Runnable{

	private ProgressHandler handler;
	private boolean abort;
	
	@Override
	public void run() {
		
		for (int i = 1; i <= 10; i++) {
			try {
				Thread.sleep(3000);
				if (abort) {
					break;
				}
				handler.updateProgress(i++);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void registerHandler(ProgressHandler handler) {
		this.handler = handler;
	}
	
	public void abort(){
		abort = true;
		handler.finish();
	}

}
