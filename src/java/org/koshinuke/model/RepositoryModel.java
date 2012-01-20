package org.koshinuke.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.koshinuke._;
import org.koshinuke.util.GitUtil;
import org.koshinuke.util.IORuntimeException;

import com.google.common.base.Function;

/**
 * @author taichi
 */
public class RepositoryModel {

	String host;

	String path;

	String name;

	@JsonSerialize(contentAs = NodeModel.class)
	List<NodeModel> branches = new ArrayList<>();
	@JsonSerialize(contentAs = NodeModel.class)
	List<NodeModel> tags = new ArrayList<>();

	public RepositoryModel() {
	}

	public RepositoryModel(String host, final Repository from) {
		this.host = host;

		File dir = from.getDirectory();
		this.path = dir.getParentFile().getName() + "/" + dir.getName();
		this.name = dir.getName();

		GitUtil.walk(from, new Function<RevWalk, _>() {
			@Override
			public _ apply(RevWalk walk) {
				try {
					this.addNodes(RepositoryModel.this.branches, walk,
							GitUtil.getBranches(from));
					this.addNodes(RepositoryModel.this.tags, walk,
							from.getTags());
				} catch (IOException e) {
					throw new IORuntimeException(e);
				}
				return _._;
			}

			void addNodes(List<NodeModel> list, RevWalk walk,
					Map<String, Ref> refs) throws IOException {
				for (String s : refs.keySet()) {
					RevCommit cmt = walk.parseCommit(refs.get(s).getObjectId());
					NodeModel nm = new NodeModel(s, s);
					nm.setLastCommit(cmt);
					list.add(nm);
				}
			}
		});
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
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

	public List<NodeModel> getBranches() {
		return this.branches;
	}

	public void setBranches(List<NodeModel> branches) {
		this.branches = branches;
	}

	public List<NodeModel> getTags() {
		return this.tags;
	}

	public void setTags(List<NodeModel> tags) {
		this.tags = tags;
	}
}
