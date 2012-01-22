package org.koshinuke.model;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author taichi
 */
public class BranchHistoryModel extends BasicGitModel {

	String path;

	String name;

	@JsonSerialize(contentAs = String[].class)
	List<String[]> activities;

	public BranchHistoryModel() {
	}

	public BranchHistoryModel(String path, String name) {
		this.path = path;
		this.name = name;
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

	public List<String[]> getActivities() {
		return this.activities;
	}

	public void setActivities(List<String[]> activities) {
		this.activities = activities;
	}

}
