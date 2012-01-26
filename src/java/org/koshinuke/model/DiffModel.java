package org.koshinuke.model;

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author taichi
 */
public class DiffModel extends BasicGitModel {

	ObjectId commit;

	ObjectId[] parents;

	@JsonSerialize(contentAs = DiffEntryModel.class)
	List<DiffEntryModel> diff = Collections.emptyList();

	public DiffModel() {
	}

	public DiffModel(RevCommit commit) {
		this.commit = commit;
		this.parents = commit.getParents();
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

	public List<DiffEntryModel> getDiff() {
		return this.diff;
	}

	public void setDiff(List<DiffEntryModel> list) {
		this.diff = list;
	}

}
