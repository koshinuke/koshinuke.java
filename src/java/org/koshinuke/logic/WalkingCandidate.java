package org.koshinuke.logic;

import org.eclipse.jgit.lib.ObjectId;
import org.koshinuke.model.NodeModel;

/**
 * @author taichi
 */
class WalkingCandidate {
	final ObjectId oid;
	final String name;
	final WalkingCandidate parent;
	final NodeModel nm;

	WalkingCandidate(ObjectId oid, String name, WalkingCandidate parent,
			NodeModel nm) {
		this.oid = oid;
		this.name = name;
		this.parent = parent;
		this.nm = nm;
	}
}