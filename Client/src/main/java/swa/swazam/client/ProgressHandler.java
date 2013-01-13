package swa.swazam.client;

public interface ProgressHandler {

	public void updateProgress(int progress);
	public void finish();
}
