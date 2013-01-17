package swa.swazam.client;

public interface ClientUI extends UICallback {

	/**
	 * Is called when the UI is needed to check its prerequisites (eg.
	 * credentials for a non-interactive UI, etc.). The UI should display any
	 * failure to the user in its native form (System.out for CLI UIs, etc.),
	 * and return a correspoding value.
	 * 
	 * @return true if prerequisites are met, false otherise
	 */
	public boolean checkPrerequisites();

	/**
	 * Is called when control is handed to the UI.
	 * 
	 * @param logic
	 *            The business logic object to be used by the UI
	 * @return The return value to be propagated to the operating system
	 */
	public int run(LogicCallback logic);

	/**
	 * Is called when the UI is needed to display a missing prerequisite. This
	 * can either be one of its own prerequisites (in which case it will
	 * probably called from within checkPrerequisites() or one of the logic's
	 * prerequisites (eg. missing server hostname/port).
	 * 
	 * @param message
	 *            A message describing the missing prerequisite
	 */
	public void displayMissingPrerequisiteMessage(String message);
}
