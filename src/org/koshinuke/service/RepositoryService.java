package org.koshinuke.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.koshinuke.model.Repository;

import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("")
public class RepositoryService {
	
	public RepositoryService() {
		System.out.println("new!!");
	}

	@GET
	@Path("/{path}/{project}")
	@Produces(MediaType.APPLICATION_JSON)
	public Repository name(@PathParam("path") String path,
			@PathParam("project") String project) {
		Repository r = new Repository();
		r.path = path;
		r.name = project;
		r.branches.add("master");
		r.branches.add("dev");
		return r;
	}

	@GET
	@Path("/{path}/{project}/tree/{branch}")
	public String tree(@PathParam("path") String path,
			@PathParam("project") String project,
			@PathParam("branch") String branch) {
		
		return path + "--" + project + "--" + branch;
	}
}
