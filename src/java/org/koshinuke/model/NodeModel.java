package org.koshinuke.model;

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

	public NodeModel() {
	}

	public NodeModel(String path, String name, RevCommit commit) {
		this.path = path;
		this.name = name;
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

}
