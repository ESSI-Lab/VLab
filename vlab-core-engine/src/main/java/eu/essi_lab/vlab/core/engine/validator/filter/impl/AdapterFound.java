package eu.essi_lab.vlab.core.engine.validator.filter.impl;

import eu.essi_lab.vlab.core.engine.services.ISourceCodeConnector;

/**
 * @author Mattia Santoro
 */
public class AdapterFound {

	private ISourceCodeConnector adapter;

	private String msg;

	public ISourceCodeConnector getAdapter() {
		return adapter;
	}

	public void setAdapter(ISourceCodeConnector adapter) {
		this.adapter = adapter;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
