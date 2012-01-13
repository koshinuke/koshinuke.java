package org.koshinuke.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.Repository;

/**
 * @author taichi
 */
public class RepositoryModel {

	String host;
	String path;
	String name;

	List<NodeModel> branches = new ArrayList<>();
	List<NodeModel> tags = new ArrayList<>();

	public RepositoryModel(String host, String project, String name) {
		this.host = host;
		this.path = project;
		this.name = name;
	}

	public RepositoryModel(String host, Repository from) {
		this.host = host;

		File dir = from.getDirectory();
		this.path = dir.getParentFile().getName();
		this.name = dir.getName();
	}
}
