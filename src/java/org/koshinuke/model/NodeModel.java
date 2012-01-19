package org.koshinuke.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevObject;
import org.koshinuke.jackson.URLdecodingDeserializer;
import org.koshinuke.jackson.URLencodingSerializer;

/**
 * @author taichi
 */
public class NodeModel extends BasicGitModel {

	@JsonIgnore
	RevObject object;

	@JsonSerialize(using = URLencodingSerializer.class)
	@JsonDeserialize(using = URLdecodingDeserializer.class)
	String path;

	@JsonSerialize(using = URLencodingSerializer.class)
	@JsonDeserialize(using = URLdecodingDeserializer.class)
	String name;

	String type;

	int children;

	public NodeModel() {
	}

	public NodeModel(String path, String name) {
		this.path = path;
		this.name = name;
	}

	@JsonIgnore
	public RevObject getObject() {
		return this.object;
	}

	@JsonIgnore
	public void setObject(RevObject object) {
		this.object = object;
		switch (object.getType()) {
		case Constants.OBJ_TREE:
			this.setType("tree");
			break;
		case Constants.OBJ_BLOB:
			this.setType("blob");
			break;
		default:
			// do nothing
			break;
		}
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

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getChildren() {
		return this.children;
	}

	public void setChildren(int children) {
		this.children = children;
	}

	public void addChildren() {
		this.children++;
	}

}
