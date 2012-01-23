package org.koshinuke.model;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author taichi
 */
public class BranchHistoryModel extends BasicGitModel {

	String path;

	String name;

	@JsonSerialize(contentAs = long[].class)
	List<long[]> activities;

	public BranchHistoryModel() {
	}

	public BranchHistoryModel(String name) {
		this.path = name;
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

	public List<long[]> getActivities() {
		return this.activities;
	}

	public void setActivities(List<long[]> activities) {
		this.activities = activities;
	}

}
