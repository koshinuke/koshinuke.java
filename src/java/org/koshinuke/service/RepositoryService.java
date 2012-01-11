package org.koshinuke.service;

import java.security.Principal;
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
import org.koshinuke.model.Auth;
import org.koshinuke.model.Repository;
import org.koshinuke.util.ServletUtil;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.resource.Singleton;

/**
 * @author taichi
 */
@Singleton
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryService {

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index(@Context HttpServletRequest req,
			@Context HttpServletResponse res) {
		Principal p = AuthenticationFilter.getUserPrincipal(req);
		if (p == null) {
			ServletUtil.redirect(res, "/login");
			return null;
		}
		return Auth.of("/repos", req.getSession(), p);
	}

	@GET
	@HeaderParam("X-Requested-With")
	@Path("/dynamic")
	public List<Repository> repolist() {
		return null;
	}

	@GET
	@HeaderParam("X-Requested-With")
	@Path("/dynamic/{project}/{repository}")
	public Repository name(@PathParam("project") String project,
			@PathParam("repository") String repository) {
		Repository r = new Repository();
		r.path = project;
		r.name = repository;
		r.branches.add("master");
		r.branches.add("dev");
		return r;
	}

	@GET
	@HeaderParam("X-Requested-With")
	@Path("/dynamic/{project}/{repository}/tree/{branch}")
	public String tree(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("branch") String branch) {

		return project + "--" + repository + "--" + branch;
	}
}
