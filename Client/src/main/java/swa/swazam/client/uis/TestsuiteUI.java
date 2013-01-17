package swa.swazam.client.uis;

import java.util.Properties;
import java.util.UUID;

import swa.swazam.client.ParameterSet;

public class TestsuiteUI extends CommandLineUI {

	public TestsuiteUI(Properties config, ParameterSet params) {
		super(config, params);
	}

	@Override
	protected String retreiveSnippetPath() {
		return params.snippetPath;
	}

	@Override
	protected void displayTimeoutMessage(UUID uuid) {}

	@Override
	protected void displaySuccessMessage(UUID uuid, String songTitle, String songArtist) {
		System.out.println(songTitle);
		System.out.println(songArtist);
	}

	@Override
	protected boolean retreiveNewQuery() {
		return false;
	}

	@Override
	public boolean checkPrerequisites() {
		if (params.snippetPath == null) {
			displayMissingPrerequisiteMessage("Testsuite UI needs a snippet path!");
			return false;
		}

		if (super.getConfigCredentials() == null) {
			displayMissingPrerequisiteMessage("Testsuite UI needs credentials in config file!");
			return false;
		}

		return true;
	}
}
