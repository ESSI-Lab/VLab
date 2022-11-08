package eu.essi_lab.vlab.core.engine.validator.filter.impl;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPIOObject;
import eu.essi_lab.vlab.core.datamodel.BPRealization;
import eu.essi_lab.vlab.core.datamodel.BPSourceCodeConventions;
import eu.essi_lab.vlab.core.datamodel.VLabDockerImage;
import eu.essi_lab.vlab.core.datamodel.VLabDockerResources;
import eu.essi_lab.vlab.core.datamodel.ValidateRealizationResponse;
import eu.essi_lab.vlab.core.datamodel.ValidationMessage;
import eu.essi_lab.vlab.core.datamodel.ValidationMessageType;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;
import eu.essi_lab.vlab.core.engine.services.ISourceCodeConventionFileLoader;
import eu.essi_lab.vlab.core.engine.validator.filter.IBPRealizationValidatorFilter;
import eu.essi_lab.vlab.core.datamodel.BPInputDescription;
import eu.essi_lab.vlab.core.datamodel.BPOutputDescription;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class VLABConventionValidator implements IBPRealizationValidatorFilter<BPRealization, ValidateRealizationResponse> {

	private Logger logger = LogManager.getLogger(VLABConventionValidator.class);

	private AdapterAvailableRealizationValidator v;
	private static final String ERROR_READING = "Error reading ";
	private static final String IN_VLAB_FOLDER = " in VLab folder: ";
	private static final String VLAB_ERROR_CODE = " VLab error code ";

	@Override
	public ValidateRealizationResponse validate(BPRealization r) {

		logger.debug("Validating VLab Convention files: {}", r.getRealizationURI());

		ValidateRealizationResponse response = new ValidateRealizationResponse();

		if (v.getAdapter() instanceof ISourceCodeConnector) {

			logger.debug("Using SourceCodeAdapter {} for {}, now validating json", v.getAdapter().getClass().getName(),
					r.getRealizationURI());

			List<ValidationMessage> msgs = doValidateJson(v.getAdapter(), v.getLoader());

			response.setMessages(msgs);

			response.setValid(msgs.stream().filter(m -> ValidationMessageType.ERROR.compareTo(m.getType()) == 0).anyMatch(match -> false));

			response.setValid(msgs.stream().filter(m -> ValidationMessageType.ERROR.compareTo(m.getType()) == 0).noneMatch(match -> true));

		} else {

			response.setValid(true);

			ValidationMessage msg = new ValidationMessage();

			msg.setType(ValidationMessageType.INFO);

			msg.setMessage("Found adapter " + v.getAdapter().getClass().getName() + " no validation performed ");

			response.setMessages(Arrays.asList(msg));

		}

		logger.debug("Completed VLABConvention Validation with result: {}", response.getValid());

		return response;
	}

	private List<ValidationMessage> doValidateJson(ISourceCodeConnector sourceCodeAdapter, ISourceCodeConventionFileLoader loader) {

		List<ValidationMessage> list = new ArrayList<>();

		ValidationMessage iomsg = new ValidationMessage();

		try {

			BPIOObject file = loader.loadIOFile();

			List<ValidationMessage> iomsgs = validIOFile(file);

			if (!iomsgs.isEmpty())
				list.addAll(iomsgs);

		} catch (BPException e) {

			logger.error("Error reading IO file from {}", sourceCodeAdapter.getDir().getAbsolutePath(), e);

			iomsg.setType(ValidationMessageType.ERROR);

			iomsg.setMessage(ERROR_READING + BPSourceCodeConventions.IO_FILE_NAME + IN_VLAB_FOLDER + e.getMessage() + VLAB_ERROR_CODE + e.getErroCode());
		}

		if (iomsg.getType() != null)
			list.add(iomsg);

		ValidationMessage dockermsg = new ValidationMessage();

		try {

			VLabDockerImage imagefile = loader.loadDockerImage();

			List<ValidationMessage> iomsgs = validImageFile(imagefile);

			if (!iomsgs.isEmpty())
				list.addAll(iomsgs);

		} catch (BPException e) {
			logger.error("Error reading Docker image file from {}", sourceCodeAdapter.getDir().getAbsolutePath(), e);

			dockermsg.setType(ValidationMessageType.ERROR);

			dockermsg.setMessage(
					ERROR_READING + BPSourceCodeConventions.DOCKER_IMAGE_FILE_NAME + IN_VLAB_FOLDER + e.getMessage() + VLAB_ERROR_CODE + e.getErroCode());
		}

		if (dockermsg.getType() != null)
			list.add(dockermsg);

		ValidationMessage scriptmsg = new ValidationMessage();

		try {

			loader.loadScripts();

		} catch (BPException e) {
			logger.error("Error reading Script file from {}", sourceCodeAdapter.getDir().getAbsolutePath(), e);

			scriptmsg.setType(ValidationMessageType.ERROR);

			scriptmsg.setMessage(ERROR_READING + BPSourceCodeConventions.SCRIPTS_FILE_NAME + IN_VLAB_FOLDER + e.getMessage() + VLAB_ERROR_CODE + e.getErroCode());
		}

		if (scriptmsg.getType() != null)
			list.add(scriptmsg);

		return list;

	}

	private List<ValidationMessage> validImageFile(VLabDockerImage imagefile) {

		List<ValidationMessage> list = new ArrayList<>();

		VLabDockerResources resources = imagefile.getResources();

		if (resources == null) {
			ValidationMessage msg = new ValidationMessage();
			msg.setType(ValidationMessageType.ERROR);
			msg.setMessage("Bad VLab convention file found " + BPSourceCodeConventions.DOCKER_IMAGE_FILE_NAME + " -> Missing mandatory field \'resources\' (NOTE "
					+ "that " + "at " + "least one of resources.cpu_units or resources.memory_mb must be present)");

			list.add(msg);
		}

		if (resources != null && isNull(resources.getMemory_mb()) && isNull(resources.getCpu_units())) {
			ValidationMessage msg = new ValidationMessage();
			msg.setType(ValidationMessageType.ERROR);
			msg.setMessage("Bad VLab convention file found " + BPSourceCodeConventions.DOCKER_IMAGE_FILE_NAME + " -> The \'resources\' field is not"
					+ " correctly set: at " + "least one of resources.cpu_units or resources.memory_mb must be present");

			list.add(msg);

		}

		return list;
	}

	private boolean isNull(String text) {
		return null == text || "".equalsIgnoreCase(text);
	}

	private List<ValidationMessage> validIOFile(BPIOObject file) {

		List<ValidationMessage> list = new ArrayList<>();

		Map<String, BPInputDescription> map = new HashMap<>();

		file.getInputs().forEach(input -> {

			BPInputDescription found = map.get(input.getInput().getId());
			if (found != null) {
				ValidationMessage msg = new ValidationMessage();
				msg.setType(ValidationMessageType.ERROR);
				msg.setMessage(
						"Found duplicate input id with id: " + input.getInput().getId() + " and names " + input.getInput().getName() + " -- " + found.getInput().getName());
				list.add(msg);
			}

			map.put(input.getInput().getId(), input);

		});

		Map<String, BPOutputDescription> map2 = new HashMap<>();

		file.getOutputs().forEach(output -> {

			BPOutputDescription found = map2.get(output.getOutput().getId());
			if (found != null) {
				ValidationMessage msg = new ValidationMessage();
				msg.setType(ValidationMessageType.ERROR);
				msg.setMessage("Found duplicate output id with id: " + output.getOutput().getId() + " and names " + output.getOutput().getName() + " -- "
						+ found.getOutput().getName());
				list.add(msg);
			}

			map2.put(output.getOutput().getId(), output);

		});

		return list;

	}

	public void setAdapterValidator(AdapterAvailableRealizationValidator validator) {
		v = validator;
	}
}
