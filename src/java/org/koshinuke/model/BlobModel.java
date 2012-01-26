package org.koshinuke.model;

import org.eclipse.jgit.lib.ObjectId;

/**
 * @author taichi
 */
public class BlobModel extends BasicGitModel {

	ObjectId objectid = ObjectId.zeroId();

	String content;

	public BlobModel() {
	}

	public BlobModel(BlobModel src) {
		super(src);
		this.objectid = src.objectid;
		this.content = src.content;
	}

	public ObjectId getObjectid() {
		return this.objectid;
	}

	public void setObjectid(ObjectId objectid) {
		this.objectid = objectid;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
