package swa.swazam.client.uis;

import java.io.File;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

import swa.swazam.client.ClientApp;
import swa.swazam.client.LogicCallback;
import swa.swazam.client.ParameterSet;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.SwazamException;

public class CommandLineUI extends TemplateUI {

	private boolean finished = false;
	private MessageDTO message = null;
	private Scanner input;
	private UUID uuid;

	public CommandLineUI(Properties config, ParameterSet params) {
		super(config, params);
	}

	@Override
	public void solved(MessageDTO message) {
		if (message.getUuid().equals(uuid)) {
			this.message = message;
			finished = true;
		}
	}

	@Override
	public void timedOut(UUID uuid) {
		if (uuid.equals(this.uuid)) {
			finished = true;
		}
	}

	@Override
	public int run(LogicCallback logic) {
		input = new Scanner(System.in);

		try {
			CredentialsDTO cred = super.getConfigCredentials();
			if (cred == null && (cred = retreiveCredentials()) == null) return ClientApp.RETURN_PARAMETERS_MISSING;

			if (!logic.login(cred)) {
				System.err.println("Could not log in with supplied credentials!");
				return ClientApp.RETURN_CREDENTIALS;
			}

			String snipPath;
			while ((snipPath = retreiveSnippetPath()) != null) {
				uuid = logic.fileChosen(new File(snipPath));

				showRequest(uuid);

				while (!finished) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException ignored) {}
				}

				if (message != null) displaySuccessMessage(uuid, message.getSongTitle(), message.getSongArtist());
				else displayTimeoutMessage(uuid);

				if (!retreiveNewQuery()) break;
			}

			if (message != null) return ClientApp.RETURN_SUCCESS;
			else return ClientApp.RETURN_NOT_FOUND;
		} catch (SwazamException e) {
			e.printStackTrace();
			return ClientApp.RETURN_GENERIC_ERROR;
		} finally {
			logic.shutdown();
		}
	}

	protected void showRequest(UUID uuid) {
		System.out.println("Fingerprint generated and sent out (" + uuid + ")");
	}

	private CredentialsDTO retreiveCredentials() {
		String user, pass;

		do {
			System.out.print("Username: ");
			user = input.nextLine();
			if (user == null) return null;
		} while (user.length() == 0);

		do {
			System.out.print("Password: ");
			pass = input.nextLine();
			if (pass == null) return null;
		} while (pass.length() == 0);

		return new CredentialsDTO(user, pass);
	}

	protected boolean retreiveNewQuery() {
		System.out.print("New query? [y/N] ");
		return "y".equalsIgnoreCase(input.nextLine());
	}

	protected void displayTimeoutMessage(UUID uuid) {
		System.out.println("Unfortunately, your request timed out, sorry (" + uuid + ")");
	}

	protected void displaySuccessMessage(UUID uuid, String songTitle, String songArtist) {
		System.out.println("Song match found! \"" + songTitle + "\" by \"" + songArtist + "\" matched (" + uuid + ")");
	}

	protected String retreiveSnippetPath() {
		System.out.print("Please enter snippet path: ");
		String path = input.nextLine();
		if (path == null || path.length() == 0) return null;
		else return path;
	}

	@Override
	public void displayMissingPrerequisiteMessage(String message) {
		System.err.println(message);
	}

}
