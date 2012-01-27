package org.koshinuke.model;

import org.eclipse.jgit.lib.ObjectId;

/**
 * @author taichi
 */
public class BlobModel extends BasicGitModel {

	ObjectId objectId = ObjectId.zeroId();

	String content;

	public BlobModel() {
	}

	public BlobModel(BlobModel src) {
		super(src);
		this.objectId = src.objectId;
		this.content = src.content;
	}

	public ObjectId getObjectId() {
		return this.objectId;
	}

	public void setObjectId(ObjectId objectid) {
		this.objectId = objectid;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
