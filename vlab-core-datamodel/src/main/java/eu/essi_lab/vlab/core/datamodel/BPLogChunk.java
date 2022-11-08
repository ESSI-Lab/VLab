package eu.essi_lab.vlab.core.datamodel;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mattia Santoro
 */
public class BPLogChunk {

	private final Set<BPLog> set;

	public BPLogChunk() {
		this(new HashSet<>());
	}

	public BPLogChunk(Set<BPLog> set) {
		this.set = set;
	}

	public Set<BPLog> getSet() {
		return set;
	}
}
