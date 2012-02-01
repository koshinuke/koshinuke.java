package org.koshinuke.model;

import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author taichi
 */
public class BlameModel extends BasicGitModel {

	String content = "";

	public BlameModel() {
	}

	public BlameModel(RevCommit commit) {
		this.setLastCommit(commit);
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
