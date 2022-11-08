package eu.essi_lab.vlab.controller.executable;

import eu.essi_lab.vlab.core.engine.factory.BPRegistriesFactory;
import org.apache.logging.log4j.LogManager;

/**
 * @author Mattia Santoro
 */
public class BPEngineCommandLine {

	private static BPEngineCommandLineExecutor executor = new BPEngineCommandLineExecutor();

	public static BPEngineCommandLineExecutor getExecutor() {
		return executor;
	}

	public static void main(String[] args) {

		BPEngineCommandLineExecutor executor = getExecutor();

		try {

			executor.setBPRegistriesFactory(BPRegistriesFactory.getFactory());
			executor.execCmd(args);

		} catch (Exception throwable) {

			LogManager.getLogger(BPEngineCommandLine.class).error("Error: {}, exit 1", throwable.getMessage(), throwable);
			System.exit(1);
		}

		LogManager.getLogger(BPEngineCommandLine.class).info("Command Executor returned with no exception, exit 0");

		System.exit(0);

	}

}
