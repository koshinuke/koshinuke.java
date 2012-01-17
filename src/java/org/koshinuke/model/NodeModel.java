package org.koshinuke.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author taichi
 */
public class NodeModel {

	String path;

	String name;

	int timestamp;

	String message;

	String author;

	String type;

	public NodeModel() {
	}

	public NodeModel(String path, String name) {
		this.path = path;
		this.name = name;
	}

	public NodeModel(String path, String name, RevCommit commit) {
		this(path, name);
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

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	public void setType(int type) {
		switch (type) {
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

	@Override
	public String toString() {
		return "NodeModel [path=" + this.path + ", name=" + this.name
				+ ", timestamp=" + this.timestamp + ", message=" + this.message
				+ ", author=" + this.author + ", type=" + this.type + "]";
	}

}
