package eu.essi_lab.vlab.core.datamodel.utils;

import eu.essi_lab.vlab.core.datamodel.BPRun;

/**
 * @author Mattia Santoro
 */
public class BPRunUtils {

	private BPRunUtils() {
		//noithing to do here
	}

	public static BPRun removeInfo(BPRun run, String requesteremail) {

		BPRun publicRun = new BPRun();

		publicRun.setPublicRun(run.isPublicRun());

		publicRun.setRunid(run.getRunid());

		publicRun.setInputs(run.getInputs());

		publicRun.setWorkflowid(run.getWorkflowid());

		publicRun.setName(run.getName());

		publicRun.setDescription(run.getDescription());

		publicRun.setCreationTime(run.getCreationTime());

		publicRun.setExecutionInfrastructure(run.getExecutionInfrastructure());

		publicRun.setOwnedbyrequester(run.getOwner() != null && run.getOwner().equalsIgnoreCase(requesteremail));

		publicRun.setSharedwithrequester(run.getSharedWith() != null && run.getSharedWith().contains(requesteremail));

		return publicRun;
	}

}
