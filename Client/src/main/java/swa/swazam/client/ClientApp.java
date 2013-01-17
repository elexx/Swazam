package swa.swazam.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import swa.swazam.client.uis.CommandLineUI;
import swa.swazam.client.uis.GraphicUI;
import swa.swazam.client.uis.TestsuiteUI;
import swa.swazam.util.exceptions.SwazamException;

public class ClientApp {

	public static final String CONFIG_USER = "credentials.user";
	public static final String CONFIG_PASSWORD = "credentials.pass";
	public static final String CONFIG_HOSTNAME = "server.hostname";
	public static final String CONFIG_PORT = "server.port";

	public static final int RETURN_SUCCESS = 0;
	public static final int RETURN_NOT_FOUND = 1;
	public static final int RETURN_PARAMETERS_MISSING = 2;
	public static final int RETURN_CREDENTIALS = 3;
	public static final int RETURN_GENERIC_ERROR = 9;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ParameterSet params = new ParameterSet();

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--cli") || args[i].equals("-i")) params.mode = Mode.CLI;
			else if (args[i].equals("--gui") || args[i].equals("-g")) params.mode = Mode.GUI;
			else if (args[i].equals("--test") || args[i].equals("-t")) params.mode = Mode.Testsuite;
			else if (args[i].equals("--config") || args[i].equals("-c")) {
				if (++i > args.length) {
					System.err.println("Missing config file path");
					return;
				}
				params.configPath = args[i];
			} else if (args[i].equals("--sample") || args[i].equals("-s")) {
				if (++i > args.length) {
					System.err.println("Missing snippet file path");
					return;
				}
				params.snippetPath = args[i];
			}
		}

		Properties config;
		try {
			config = loadConfig(params.configPath);
		} catch (IOException e) {
			System.err.println("Config file could not be read: " + e.getMessage());
			e.printStackTrace();
			return;
		}

		ClientUI ui = null;
		switch (params.mode) {
			case CLI:
				ui = new CommandLineUI(config, params);
				break;

			case GUI:
				ui = new GraphicUI(config, params);
				break;

			case Testsuite:
				ui = new TestsuiteUI(config, params);
				break;

			default:
				throw new InternalError("Invalid UI mode selected");
		}

		if (!ui.checkPrerequisites()) System.exit(RETURN_PARAMETERS_MISSING);

		ClientLogic logic;

		try {
			logic = new ClientLogic(config, params, ui);
		} catch (InsufficientParametersException ipEx) {
			ui.displayMissingPrerequisiteMessage(ipEx.getMessage());
			System.exit(ClientApp.RETURN_PARAMETERS_MISSING);
			return;
		} catch (SwazamException e) {
			ui.displayMissingPrerequisiteMessage("An error occured while starting up: " + e.getMessage());
			e.printStackTrace();
			System.exit(ClientApp.RETURN_GENERIC_ERROR);
			return;
		}

		int returnValue = ui.run(logic);

		System.exit(returnValue);
	}

	private static Properties loadConfig(String configPath) throws IOException {
		Properties configFile = new Properties();
		if (configPath != null) {
			configFile.load(new FileInputStream(configPath));
		} else {
			configFile.load(ClientApp.class.getClassLoader().getResourceAsStream("client.properties"));
		}
		return configFile;
	}

	public enum Mode {
		CLI, GUI, Testsuite
	}

}
