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

	@JsonIgnore
	String beforePath;

	@JsonIgnore
	String afterPath;

	public DiffEntryModel() {
	}

	public DiffEntryModel(DiffEntry de) {
		this.operation = de.getChangeType();
		this.beforePath = de.getOldPath();
		this.afterPath = de.getNewPath();
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

	@JsonProperty("b_path")
	public String getBeforePath() {
		return this.beforePath;
	}

	@JsonProperty("b_path")
	public void setBeforePath(String beforePath) {
		this.beforePath = beforePath;
	}

	@JsonProperty("a_path")
	public String getAfterPath() {
		return this.afterPath;
	}

	@JsonProperty("a_path")
	public void setAfterPath(String afterPath) {
		this.afterPath = afterPath;
	}

}
