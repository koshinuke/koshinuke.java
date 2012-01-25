package org.koshinuke.model;

import org.eclipse.jgit.lib.ObjectId;

/**
 * @author taichi
 */
public class BlobModel extends BasicGitModel {

	ObjectId objectid = ObjectId.zeroId();

	String contents;

	public BlobModel() {
	}

	public BlobModel(BlobModel src) {
		super(src);
		this.objectid = src.objectid;
		this.contents = src.contents;
	}

	public ObjectId getObjectid() {
		return this.objectid;
	}

	public void setObjectid(ObjectId objectid) {
		this.objectid = objectid;
	}

	public String getContents() {
		return this.contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
}
