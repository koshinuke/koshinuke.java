package org.koshinuke.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;

/**
 * @author taichi
 */
public class NodeModel {

	@JsonIgnore
	RevObject object;

	String path;

	String name;

	int timestamp;

	String message;

	String author;

	String type;

	int children;

	public NodeModel() {
	}

	public NodeModel(String path, String name) {
		this.path = path;
		this.name = name;
	}

	@JsonIgnore
	public RevObject getObject() {
		return this.object;
	}

	@JsonIgnore
	public void setObject(RevObject object) {
		this.object = object;
		switch (object.getType()) {
		case Constants.OBJ_TREE:
			this.setType("tree");
			break;
		case Constants.OBJ_BLOB:
			this.setType("blob");
			break;
		default:
			// do nothing
			break;
		}
	}

	@JsonIgnore
	public void setLastCommit(RevCommit commit) {
		this.timestamp = commit.getCommitTime();
		this.message = commit.getFullMessage();
		this.author = commit.getAuthorIdent().getName();
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getChildren() {
		return this.children;
	}

	public void setChildren(int children) {
		this.children = children;
	}

	public void addChildren() {
		this.children++;
	}

	@Override
	public String toString() {
		return "NodeModel [path=" + this.path + ", name=" + this.name
				+ ", timestamp=" + this.timestamp + ", message=" + this.message
				+ ", author=" + this.author + ", type=" + this.type
				+ ", children=" + this.children + "]";
	}

}
