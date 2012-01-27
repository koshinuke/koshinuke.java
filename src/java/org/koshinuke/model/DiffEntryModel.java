package org.koshinuke.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

/**
 * @author taichi
 */
public class DiffEntryModel {

	String oldContent;

	String newContent;

	String patch;

	@JsonIgnore
	ChangeType operation;

	String oldPath;

	String newPath;

	public DiffEntryModel() {
	}

	public DiffEntryModel(DiffEntry de) {
		this.operation = de.getChangeType();
		this.oldPath = de.getOldPath();
		this.newPath = de.getNewPath();
	}

	public String getOldContent() {
		return this.oldContent;
	}

	public void setOldContent(String content) {
		this.oldContent = content;
	}

	public String getNewContent() {
		return this.newContent;
	}

	public void setNewContent(String newContent) {
		this.newContent = newContent;
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

	public String getOldPath() {
		return this.oldPath;
	}

	public void setOldPath(String oldpath) {
		this.oldPath = oldpath;
	}

	public String getNewPath() {
		return this.newPath;
	}

	public void setNewPath(String newpath) {
		this.newPath = newpath;
	}

}
