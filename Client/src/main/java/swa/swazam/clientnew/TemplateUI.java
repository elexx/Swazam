package swa.swazam.clientnew;

import java.util.Properties;

import swa.swazam.util.dto.CredentialsDTO;

public abstract class TemplateUI implements ClientUI {

	protected Properties config;
	protected ParameterSet params;

	public TemplateUI(Properties config, ParameterSet params) {
		this.config = config;
		this.params = params;
	}

	public CredentialsDTO getConfigCredentials() {
		return getConfigCredentials(config);
	}

	public static CredentialsDTO getConfigCredentials(Properties config) {
		if (config.containsKey(App.CONFIG_USER) && config.containsKey(App.CONFIG_PASSWORD)) return new CredentialsDTO(config.getProperty(App.CONFIG_USER),
				config.getProperty(App.CONFIG_PASSWORD));
		else return null;
	}

	@Override
	public boolean checkPrerequisites() {
		return true;
	}
}
