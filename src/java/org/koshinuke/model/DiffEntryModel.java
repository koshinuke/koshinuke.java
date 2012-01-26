package org.koshinuke.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

/**
 * @author taichi
 */
public class DiffEntryModel {

	String content;

	String patch;

	@JsonIgnore
	ChangeType operation;

	String oldpath;

	String newpath;

	public DiffEntryModel() {
	}

	public DiffEntryModel(DiffEntry de) {
		this.operation = de.getChangeType();
		this.oldpath = de.getOldPath();
		this.newpath = de.getNewPath();
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPatch() {
		return this.patch;
	}

	public void setPatch(String patch) {
		this.patch = patch;
	}

	@JsonProperty("operation")
	public String getOperation() {
		return this.operation.name().toLowerCase();
	}

	@JsonProperty("operation")
	public void setOperation(String operation) {
		this.operation = ChangeType.valueOf(operation.toUpperCase());
	}

	public String getOldpath() {
		return this.oldpath;
	}

	public void setOldpath(String oldpath) {
		this.oldpath = oldpath;
	}

	public String getNewpath() {
		return this.newpath;
	}

	public void setNewpath(String newpath) {
		this.newpath = newpath;
	}

}
