package org.koshinuke.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.StringUtils;

/**
 * @author taichi
 */
public class DiffModel {

	@JsonIgnore
	ObjectId commit;

	@JsonIgnore
	List<ObjectId> parents;

	@JsonSerialize(contentAs = DiffEntryModel.class)
	List<DiffEntryModel> diff;

	public DiffModel() {
	}

	public DiffModel(RevCommit commit) {
		this.commit = commit;
		this.setRawParents(commit.getParents());
	}

	@JsonIgnore
	public ObjectId getRawCommit() {
		return this.commit;
	}

	@JsonIgnore
	public void setRawCommit(ObjectId commit) {
		this.commit = commit;
	}

	@JsonProperty("commit")
	public String getCommit() {
		return this.commit.name();
	}

	@JsonProperty("commit")
	public void setCommit(String commitId) {
		if (StringUtils.isEmptyOrNull(commitId) == false
				&& commitId.length() == Constants.OBJECT_ID_STRING_LENGTH) {
			this.commit = ObjectId.fromString(commitId);
		}
	}

	@JsonIgnore
	public void setRawParents(RevCommit[] parents) {
		if (parents != null) {
			List<ObjectId> newone = new ArrayList<>(parents.length);
			for (RevCommit rc : parents) {
				newone.add(rc.getId());
			}
			this.parents = newone;
		}
	}

	@JsonProperty("parents")
	@JsonSerialize(contentAs = String.class)
	public List<String> getParents() {
		if (this.parents == null || this.parents.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> newone = new ArrayList<>(this.parents.size());
		for (ObjectId oid : this.parents) {
			newone.add(oid.name());
		}
		return newone;
	}

	@JsonProperty("parents")
	@JsonSerialize(contentAs = String.class)
	public void setParents(List<String> list) {
		if (list != null && 0 < list.size()) {
			List<ObjectId> result = new ArrayList<>();
			for (String s : list) {
				if (StringUtils.isEmptyOrNull(s) == false
						&& s.length() == Constants.OBJECT_ID_STRING_LENGTH) {
					result.add(ObjectId.fromString(s));
				}
			}
		}
	}

	public List<DiffEntryModel> getDiff() {
		return this.diff;
	}

	public void setDiff(List<DiffEntryModel> diff) {
		this.diff = diff;
	}

}
