package org.koshinuke.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.util.StringUtils;

/**
 * @author taichi
 */
public class BlobModel extends BasicGitModel {

	@JsonIgnore
	ObjectId commitId;

	String contents;

	public BlobModel() {
	}

	@JsonIgnore
	public ObjectId getCommitId() {
		return this.commitId;
	}

	@JsonIgnore
	public void setCommitId(ObjectId commitId) {
		this.commitId = commitId;
	}

	@JsonProperty("commit")
	public String getCommit() {
		if (this.commitId == null) {
			return ObjectId.zeroId().name();
		}
		return this.commitId.name();
	}

	@JsonProperty("commit")
	public void setCommit(String commit) {
		if (StringUtils.isEmptyOrNull(commit) == false
				&& commit.length() == Constants.OBJECT_ID_STRING_LENGTH) {
			this.commitId = ObjectId.fromString(commit);
		}
	}

	public String getContents() {
		return this.contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

}
