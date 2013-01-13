package swa.swazam.client;

public class TestRunnable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				System.out.println("Thread started");
				try {
					Thread.sleep(5000);
					System.exit(0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		System.out.println("Main");
		r.run();
	}

}
