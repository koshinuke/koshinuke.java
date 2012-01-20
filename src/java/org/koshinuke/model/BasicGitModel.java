package org.koshinuke.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author taichi
 */
public class BasicGitModel {

	protected int timestamp;

	protected String message;

	protected String author;

	public BasicGitModel() {
	}

	public BasicGitModel(BasicGitModel src) {
		this.timestamp = src.timestamp;
		this.message = src.message;
		this.author = src.author;
	}

	@JsonIgnore
	public void setLastCommit(RevCommit commit) {
		this.timestamp = commit.getCommitTime();
		this.message = commit.getFullMessage();
		this.author = commit.getAuthorIdent().getName();
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
