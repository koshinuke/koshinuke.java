package org.koshinuke.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.eclipse.jgit.revwalk.RevCommit;
import org.koshinuke.util.ServletUtil;

/**
 * @author taichi
 */
public class BasicGitModel {

	protected int timestamp;
	protected String message;
	protected String author;

	public BasicGitModel() {
	}

	@JsonIgnore
	public void setLastCommit(RevCommit commit) {
		this.timestamp = commit.getCommitTime();
		this.message = ServletUtil.encode(commit.getFullMessage());
		this.author = ServletUtil.encode(commit.getAuthorIdent().getName());
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
