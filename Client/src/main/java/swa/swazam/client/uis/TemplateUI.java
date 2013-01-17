package swa.swazam.client.uis;

import java.util.Properties;

import swa.swazam.client.ClientApp;
import swa.swazam.client.ClientUI;
import swa.swazam.client.ParameterSet;
import swa.swazam.util.dto.CredentialsDTO;

abstract class TemplateUI implements ClientUI {

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
		if (config.containsKey(ClientApp.CONFIG_USER) && config.containsKey(ClientApp.CONFIG_PASSWORD)) return new CredentialsDTO(config.getProperty(ClientApp.CONFIG_USER),
				config.getProperty(ClientApp.CONFIG_PASSWORD));
		else return null;
	}

	@Override
	public boolean checkPrerequisites() {
		return true;
	}
}
