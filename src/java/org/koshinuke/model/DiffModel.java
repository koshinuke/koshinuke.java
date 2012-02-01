package org.koshinuke.model;

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author taichi
 */
public class DiffModel extends CommitModel {

	@JsonSerialize(contentAs = DiffEntryModel.class)
	List<DiffEntryModel> diff = Collections.emptyList();

	public DiffModel() {
	}

	public DiffModel(RevCommit commit) {
		super(commit);
	}

	public void setDiff(List<DiffEntryModel> list) {
		this.diff = list;
	}

	public List<DiffEntryModel> getDiff() {
		return this.diff;
	}
}
