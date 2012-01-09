package org.koshinuke.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.koshinuke.filter.AuthenticationFilter;
import org.koshinuke.model.Repository;
import org.koshinuke.soy.RepoSoyInfo;
import org.koshinuke.soy.SoyTemplatesModule;
import org.koshinuke.util.ServletUtil;

import com.sun.jersey.api.view.Viewable;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryService {

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index(@Context HttpServletRequest request,
			@Context HttpServletResponse res) {
		if (AuthenticationFilter.isLoggedIn(request) == false) {
			ServletUtil.redirect(res, "/login");
			return null;
		}
		return SoyTemplatesModule.of(RepoSoyInfo.HOME);
	}

	@GET
	@HeaderParam("X-Requested-With")
	@Path("/dynamic")
	public List<Repository> repolist() {
		return null;
	}

	@GET
	@Path("/dynamic/{project}/{repository}")
	// @HeaderParam("X-Requested-With")
	public Repository name(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		Repository r = new Repository();
		r.path = repository;
		r.name = project;
		r.branches.add("master");
		r.branches.add("dev");
		return r;
	}

	@GET
	@Path("/dynamic/{project}/{repository}/tree/{branch}")
	@HeaderParam("X-Requested-With")
	public String tree(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("branch") String branch) {

		return project + "--" + repository + "--" + branch;
	}
}
