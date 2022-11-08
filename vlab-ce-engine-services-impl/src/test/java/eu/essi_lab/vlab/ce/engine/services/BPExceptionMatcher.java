package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.core.datamodel.BPException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Mattia Santoro
 */
public class BPExceptionMatcher extends BaseMatcher<BPException> {

	boolean codeok = false;
	boolean msgok = false;
	private final BPException.ERROR_CODES code;
	private final String msg;

	public BPExceptionMatcher(String expectedMsg, BPException.ERROR_CODES errorCode) {
		this.msg = expectedMsg;
		this.code = errorCode;
	}

	@Override
	public boolean matches(Object o) {

		if (!(o instanceof BPException))
			return false;

		codeok = ((BPException) o).getErroCode() != null && ((BPException) o).getErroCode() - this.code.getCode() == 0;
		msgok = ((BPException) o).getMessage() != null && ((BPException) o).getMessage().toLowerCase().contains(this.msg.toLowerCase());

		return codeok && msgok;

	}

	@Override
	public void describeTo(Description description) {

		description.appendText("A BPException with code " + this.code + " and message containing: " + this.msg);

	}
}