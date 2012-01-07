package org.koshinuke.service;

import java.security.Principal;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.koshinuke.filter.AuthenticationFilter;
import org.koshinuke.model.Repository;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryService {

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index(@Context HttpServletRequest req) {
		if (AuthenticationFilter.isLoggedIn(req)) {
			return new Viewable("/repo");
		}
		return new Viewable("/login");
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Viewable login(@Context HttpServletRequest req,
			@FormParam("u") String u, @FormParam("p") String p) {
		try {
			req.login(u, p);
			HttpSession session = req.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			session = req.getSession(true);
			Principal principal = req.getUserPrincipal();
			session.setAttribute(AuthenticationFilter.AUTH, principal);
		} catch (ServletException e) {
			// login failed
		}
		return index(req);
	}

	@GET
	@HeaderParam("X-Requested-With")
	public List<Repository> repolist() {
		return null;
	}

	@GET
	@Path("/{path}/{project}")
	@HeaderParam("X-Requested-With")
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
	@HeaderParam("X-Requested-With")
	public String tree(@PathParam("path") String path,
			@PathParam("project") String project,
			@PathParam("branch") String branch) {

		return path + "--" + project + "--" + branch;
	}
}
