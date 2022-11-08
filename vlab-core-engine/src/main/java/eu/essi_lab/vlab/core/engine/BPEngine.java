package eu.essi_lab.vlab.core.engine;

import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPInput;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.engine.services.IBPRunRegistry;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class BPEngine {

	private IBPRunRegistry bpRunRegistry;

	private Logger logger = LogManager.getLogger(BPEngine.class);

	//TODO should user be here?
	public BPRun execute(String ebpIdentifier, List<BPInput> bpInputs, BPUser user, String runName, String runDescription,
			String infrastructureid) throws BPException {
		String inputsLog = logInputs(bpInputs);

		logger.info("Requested execution of workflow {} with user inputs: {}", ebpIdentifier, inputsLog);

		BPRun run = new BPRun();

		run.setInputs(bpInputs);

		run.setWorkflowid(ebpIdentifier);

		run.setOwner(user.getEmail());

		run.setCreationTime(new Date().getTime());

		run.setName(runName);

		run.setDescription(runDescription);

		run.setExecutionInfrastructure(infrastructureid);

		BPRun registered = bpRunRegistry.registerBPRun(run);

		logger.info("Registered run {} for execution of workflow {} with user inputs: {}", run.getRunid(), ebpIdentifier, inputsLog);

		return registered;

	}

	public BPRun execute(String ebpIdentifier, List<BPInput> bpInputs, BPUser user, String runName, String runDescription)
			throws BPException {

		return execute(ebpIdentifier, bpInputs, user, runName, runDescription, null);
	}

	private String logInputs(List<BPInput> bpInputs) {

		if (bpInputs == null || bpInputs.isEmpty())
			return "none";

		StringBuilder sb = new StringBuilder();

		for (BPInput input : bpInputs)
			sb.append("[").append(input.getName()).append(":").append(input.getValue()).append("] ");

		return sb.toString();

	}

	public void setBpRunRegistry(IBPRunRegistry bpRunRegistry) {
		this.bpRunRegistry = bpRunRegistry;
	}

}
