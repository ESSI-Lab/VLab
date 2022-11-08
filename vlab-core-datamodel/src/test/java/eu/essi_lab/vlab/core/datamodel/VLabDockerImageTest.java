package eu.essi_lab.vlab.core.datamodel;

import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class VLabDockerImageTest {

	@Test
	public void serialize() throws BPException {

		VLabDockerImage image = new VLabDockerImage();

		String testimage = "testimage";
		image.setImage(testimage);

		VLabDockerContainer container = new VLabDockerContainer();
		List<String> commands = Arrays.asList(new String[] {
				"source activate despeckle && python /efsmodels/Guided_filter_despeckling_modified.py -i /data/SAR_image.tif -g /data/RGB_image.tif -m /data/Cloud_mask.tif -o /data/out.tif" });

		container.setCommand(commands);

		List<String> enrtry = Arrays.asList(new String[] { "/bin/bash", "-c" });

		container.setEntryPoint(enrtry);

		image.setContainer(container);

		VLabDockerResources resources = new VLabDockerResources();
		resources.setMemory_mb("2000");
		resources.setCpu_units("2");
		image.setResources(resources);

		String serialied = new JSONSerializer().serialize(image);
		System.out.println(serialied);

		VLabDockerImage deserialized = new JSONDeserializer().deserialize(serialied, VLabDockerImage.class);

		assertEquals(deserialized.getImage(), testimage);

		assertEquals(deserialized.getContainer().getCommand(), commands);

	}

	@Test
	public void deserialize() throws BPException {

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test_dockerImage.json");

		assertNotNull(stream);

		VLabDockerImage image = new JSONDeserializer().deserialize(stream, VLabDockerImage.class);

		assertEquals("repository/anaconda:0.1.0", image.getImage());

		assertEquals(Arrays.asList(new String[] { "/bin/bash", "-c" }), image.getContainer().getEntryPoint());

		assertEquals(Arrays.asList(new String[] { "source activate test", "modelrun.sh" }), image.getContainer().getCommand());

	}

}