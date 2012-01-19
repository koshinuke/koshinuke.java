package org.koshinuke.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.eclipse.jgit.revwalk.RevCommit;
import org.koshinuke.jackson.URLdecodingDeserializer;
import org.koshinuke.jackson.URLencodingSerializer;

/**
 * @author taichi
 */
public class BasicGitModel {

	protected int timestamp;

	@JsonSerialize(using = URLencodingSerializer.class)
	@JsonDeserialize(using = URLdecodingDeserializer.class)
	protected String message;

	@JsonSerialize(using = URLencodingSerializer.class)
	@JsonDeserialize(using = URLdecodingDeserializer.class)
	protected String author;

	public BasicGitModel() {
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
