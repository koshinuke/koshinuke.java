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
	ObjectId objectid;

	String contents;

	public BlobModel() {
	}

	public BlobModel(BlobModel src) {
		super(src);
		this.objectid = src.objectid;
		this.contents = src.contents;
	}

	@JsonIgnore
	public void setRawObjectId(ObjectId objectid) {
		this.objectid = objectid;
	}

	@JsonIgnore
	public ObjectId getRawObjectId() {
		return this.objectid;
	}

	@JsonProperty("objectid")
	public String getObjectId() {
		if (this.objectid == null) {
			return ObjectId.zeroId().name();
		}
		return this.objectid.name();
	}

	@JsonProperty("objectid")
	public void setObjectId(String objectid) {
		if (StringUtils.isEmptyOrNull(objectid) == false
				&& objectid.length() == Constants.OBJECT_ID_STRING_LENGTH) {
			this.objectid = ObjectId.fromString(objectid);
		}
	}

	public String getContents() {
		return this.contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
}
