package eu.essi_lab.vlab.controller;

import eu.essi_lab.vlab.core.datamodel.BPException;
import java.util.Optional;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Mattia Santoro
 */
public class BPExceptionMatcher extends BaseMatcher<BPException> {

	boolean codeok = false;
	boolean msgok = false;
	private final Optional<BPException.ERROR_CODES> code;
	private final String msg;

	public BPExceptionMatcher(String expectedMsg, BPException.ERROR_CODES errorCode) {
		this.msg = expectedMsg;
		this.code = Optional.ofNullable(errorCode);
	}

	@Override
	public boolean matches(Object o) {

		if (!(o instanceof BPException))
			return false;

		codeok = true;
		if (this.code.isPresent())
			codeok = ((BPException) o).getErroCode() != null && ((BPException) o).getErroCode() - this.code.get().getCode() == 0;

		msgok = ((BPException) o).getMessage() != null && ((BPException) o).getMessage().toLowerCase().contains(this.msg.toLowerCase());

		return codeok && msgok;

	}

	@Override
	public void describeTo(Description description) {

		description.appendText("A BPException with code " + this.code + " and message containing: " + this.msg);

	}
}