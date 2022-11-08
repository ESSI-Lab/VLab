package eu.essi_lab.vlab.ce.controller.services.ecs;

import eu.essi_lab.vlab.core.datamodel.VLabDockerContainer;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.ecs.model.ContainerDefinition;
import software.amazon.awssdk.services.ecs.model.LogConfiguration;
import software.amazon.awssdk.services.ecs.model.TaskDefinition;

/**
 * @author Mattia Santoro
 */
public class TaskDefinitionMatcherTest {

	/**
	 * <pre>
	 *     <ul>
	 *         <li>Image: imageName imageName</li>
	 *         <li>Entry: none none</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testImageNameOnly() {

		TaskDefinition task = Mockito.mock(TaskDefinition.class);
		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		String imageName = "imageName";
		Mockito.when(image.getImage()).thenReturn(imageName);

		ContainerDefinition containerDef = Mockito.mock(ContainerDefinition.class);

		Mockito.when(containerDef.image()).thenReturn(imageName);

		List<ContainerDefinition> containerDefinitions = Arrays.asList(new ContainerDefinition[] { containerDef });
		Mockito.when(task.containerDefinitions()).thenReturn(containerDefinitions);
		LogConfiguration logConf = Mockito.mock(LogConfiguration.class);
		Mockito.doReturn(logConf).when(containerDef).logConfiguration();

		TaskDefinitionMatcher matcher = new TaskDefinitionMatcher(task);

		assertTrue(matcher.match(image));

	}

	/**
	 * <pre>
	 *     <ul>
	 *         <li>Image: imageName-fail imageName</li>
	 *         <li>Entry: none none</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testImageNameOnlyFail() {

		TaskDefinition task = Mockito.mock(TaskDefinition.class);
		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		String imageName = "imageName";
		Mockito.when(image.getImage()).thenReturn(imageName + "-fail");

		ContainerDefinition containerDef = Mockito.mock(ContainerDefinition.class);

		Mockito.when(containerDef.image()).thenReturn(imageName);

		List<ContainerDefinition> containerDefinitions = Arrays.asList(new ContainerDefinition[] { containerDef });
		Mockito.when(task.containerDefinitions()).thenReturn(containerDefinitions);

		TaskDefinitionMatcher matcher = new TaskDefinitionMatcher(task);

		assertFalse(matcher.match(image));

	}

	/**
	 * <pre>
	 *     <ul>
	 *         <li>Image: imageName-fail imageName</li>
	 *         <li>Entry: entry none</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDifferentImageNameDockerHasEntryPoint() {

		TaskDefinition task = Mockito.mock(TaskDefinition.class);
		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		String imageName = "imageName";
		Mockito.when(image.getImage()).thenReturn(imageName + "-fail");

		ContainerDefinition containerDef = Mockito.mock(ContainerDefinition.class);

		Mockito.when(containerDef.image()).thenReturn(imageName);

		List<ContainerDefinition> containerDefinitions = Arrays.asList(new ContainerDefinition[] { containerDef });
		Mockito.when(task.containerDefinitions()).thenReturn(containerDefinitions);

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		Mockito.when(container.getEntryPoint()).thenReturn(Arrays.asList(new String[] { "/bin/sh", "-c" }));

		Mockito.when(image.getContainer()).thenReturn(container);

		TaskDefinitionMatcher matcher = new TaskDefinitionMatcher(task);

		assertFalse(matcher.match(image));

	}

	/**
	 * <pre>
	 *     <ul>
	 *         <li>Image: imageName imageName</li>
	 *         <li>Entry: none entry</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testSameImageNameTaskHasEntryPoint() {

		TaskDefinition task = Mockito.mock(TaskDefinition.class);
		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		String imageName = "imageName";
		Mockito.when(image.getImage()).thenReturn(imageName);

		ContainerDefinition containerDef = Mockito.mock(ContainerDefinition.class);

		Mockito.when(containerDef.image()).thenReturn(imageName + "-fail");

		List<String> entryPoint = Arrays.asList(new String[] { "/bin/sh", "-c" });
		Mockito.when(containerDef.entryPoint()).thenReturn(entryPoint);

		List<ContainerDefinition> containerDefinitions = Arrays.asList(new ContainerDefinition[] { containerDef });
		Mockito.when(task.containerDefinitions()).thenReturn(containerDefinitions);

		TaskDefinitionMatcher matcher = new TaskDefinitionMatcher(task);

		assertFalse(matcher.match(image));

	}

	/**
	 * <pre>
	 *     <ul>
	 *         <li>Image: imageName imageName</li>
	 *         <li>Entry: entry none</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testSameImageNameDockerHasEntryPoint() {

		TaskDefinition task = Mockito.mock(TaskDefinition.class);
		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		String imageName = "imageName";
		Mockito.when(image.getImage()).thenReturn(imageName);

		ContainerDefinition containerDef = Mockito.mock(ContainerDefinition.class);

		Mockito.when(containerDef.image()).thenReturn(imageName);

		List<ContainerDefinition> containerDefinitions = Arrays.asList(new ContainerDefinition[] { containerDef });
		Mockito.when(task.containerDefinitions()).thenReturn(containerDefinitions);

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		Mockito.when(container.getEntryPoint()).thenReturn(Arrays.asList(new String[] { "/bin/sh", "-c" }));

		Mockito.when(image.getContainer()).thenReturn(container);

		TaskDefinitionMatcher matcher = new TaskDefinitionMatcher(task);

		assertFalse(matcher.match(image));

	}

	/**
	 * <pre>
	 *     <ul>
	 *         <li>Image: imageName imageName</li>
	 *         <li>Entry: entry entry</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testSameImageNameAndEntryPoint() {

		TaskDefinition task = Mockito.mock(TaskDefinition.class);
		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		String imageName = "imageName";
		List<String> entryPoint = Arrays.asList(new String[] { "/bin/sh", "-c" });

		Mockito.when(image.getImage()).thenReturn(imageName);

		ContainerDefinition containerDef = Mockito.mock(ContainerDefinition.class);

		Mockito.when(containerDef.image()).thenReturn(imageName);

		Mockito.when(containerDef.entryPoint()).thenReturn(entryPoint);

		LogConfiguration logConf = Mockito.mock(LogConfiguration.class);
		Mockito.doReturn(logConf).when(containerDef).logConfiguration();

		List<ContainerDefinition> containerDefinitions = Arrays.asList(new ContainerDefinition[] { containerDef });
		Mockito.when(task.containerDefinitions()).thenReturn(containerDefinitions);

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		Mockito.when(container.getEntryPoint()).thenReturn(entryPoint);

		Mockito.when(image.getContainer()).thenReturn(container);

		TaskDefinitionMatcher matcher = new TaskDefinitionMatcher(task);

		assertTrue(matcher.match(image));

	}

	/**
	 * <pre>
	 *     <ul>
	 *         <li>Image: imageName-fail imageName</li>
	 *         <li>Entry: entry none</li>
	 *         <li>WoDir: none wdir</li>
	 *     </ul>
	 * </pre>
	 */
	@Test
	public void testDifferentImageNameDockerHasEntryPointTaskHasWDir() {

		TaskDefinition task = Mockito.mock(TaskDefinition.class);
		VLabDockerImage image = Mockito.mock(VLabDockerImage.class);

		String imageName = "imageName";
		Mockito.when(image.getImage()).thenReturn(imageName + "-fail");

		ContainerDefinition containerDef = Mockito.mock(ContainerDefinition.class);

		Mockito.when(containerDef.image()).thenReturn(imageName);

		List<ContainerDefinition> containerDefinitions = Arrays.asList(new ContainerDefinition[] { containerDef });
		Mockito.when(task.containerDefinitions()).thenReturn(containerDefinitions);

		VLabDockerContainer container = Mockito.mock(VLabDockerContainer.class);

		Mockito.when(container.getEntryPoint()).thenReturn(Arrays.asList(new String[] { "/bin/sh", "-c" }));

		Mockito.when(image.getContainer()).thenReturn(container);

		TaskDefinitionMatcher matcher = new TaskDefinitionMatcher(task);

		assertFalse(matcher.match(image));

	}

}