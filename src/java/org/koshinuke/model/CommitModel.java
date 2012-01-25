package org.koshinuke.model;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author taichi
 */
public class CommitModel extends BasicGitModel {

	ObjectId commit = ObjectId.zeroId();

	ObjectId[] parents = null;

	public CommitModel() {
	}

	public CommitModel(RevCommit commit) {
		this.commit = commit.getId();
		this.parents = commit.getParents();
		this.setLastCommit(commit);
	}

	public ObjectId getCommit() {
		return this.commit;
	}

	public void setCommit(ObjectId commit) {
		this.commit = commit;
	}

	public ObjectId[] getParents() {
		return this.parents;
	}

	public void setParents(ObjectId[] parents) {
		this.parents = parents;
	}

}
