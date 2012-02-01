package org.koshinuke.model;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author taichi
 */
public class CommitModel extends BasicGitModel {

	ObjectId[] parents = null;

	public CommitModel() {
	}

	public CommitModel(RevCommit commit) {
		this.parents = commit.getParents();
		this.setLastCommit(commit);
	}

	public ObjectId[] getParents() {
		return this.parents;
	}

	public void setParents(ObjectId[] parents) {
		this.parents = parents;
	}

}
