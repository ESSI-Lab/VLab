package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.core.datamodel.VLabDockerContainer;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.ecs.model.ContainerDefinition;
import software.amazon.awssdk.services.ecs.model.TaskDefinition;

/**
 * @author Mattia Santoro
 */
public class TaskDefinitionMatcher {

	private final TaskDefinition task;
	private Logger logger = LogManager.getLogger(TaskDefinitionMatcher.class);

	public TaskDefinitionMatcher(TaskDefinition taskDef) {

		task = taskDef;
	}

	public boolean match(VLabDockerImage dockerImage) {

		if (!checkLogConf()) {
			logger.trace("No Logger definition was found, returning false.");
			return false;
		}

		if (!matchImageName(dockerImage.getImage())) {
			logger.trace("Image name mismatch, returning false.");
			return false;
		}

		if (!matchentryPoint(dockerImage.getContainer())) {
			logger.trace("Entry point name mismatch, returning false.");
			return false;
		}

		return true;
	}

	private boolean checkLogConf() {

		if (task.containerDefinitions().isEmpty())
			return false;

		ContainerDefinition definition = task.containerDefinitions().get(0);

		return definition.logConfiguration() != null;

	}

	private boolean isTaskEntryPointNull() {
		List<String> taskEntryPoint = task.containerDefinitions().get(0).entryPoint();

		if (taskEntryPoint == null)
			return true;

		return taskEntryPoint.isEmpty();
	}

	private boolean matchentryPoint(VLabDockerContainer container) {

		if (container == null || container.getEntryPoint() == null) {

			return isTaskEntryPointNull();

		}

		List<String> entryPoint = container.getEntryPoint();

		if (entryPoint == null || entryPoint.isEmpty())
			return isTaskEntryPointNull();

		List<String> taskEntryPoint = task.containerDefinitions().get(0).entryPoint();

		if (taskEntryPoint == null)
			return false;

		if (taskEntryPoint.size() != entryPoint.size())
			return false;

		for (var i = 0; i < taskEntryPoint.size(); i++) {
			if (!entryPoint.get(i).equalsIgnoreCase(taskEntryPoint.get(i)))
				return false;
		}

		return true;
	}

	private boolean matchImageName(String image) {

		return task.containerDefinitions().get(0).image().equalsIgnoreCase(image);

	}

}
