package org.koshinuke.service;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.koshinuke.filter.AuthenticationFilter;
import org.koshinuke.model.Repository;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("")
public class RepositoryService {

	public RepositoryService() {
		System.out.println("new!!");
	}

	@GET
	public Viewable index(@Context ServletContext ctx,
			@Context HttpServletRequest req) throws IOException {
		if (AuthenticationFilter.isLoggedIn(req)) {
			return new Viewable("/repo.html");
		}
		return new Viewable("/login.html");
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response login(@Context HttpServletRequest req,
			@Context HttpServletResponse res, @QueryParam("u") String u,
			@QueryParam("p") String p) throws IOException {
		System.out.println("login");
		try {
			req.login(u, p);
			System.out.println("success !!");
			HttpSession session = req.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			session = req.getSession(true);
			Principal principal = req.getUserPrincipal();
			System.out.println(principal);
			session.setAttribute(AuthenticationFilter.AUTH, principal);
			res.sendRedirect("/");
		} catch (ServletException e) {
			e.printStackTrace();
			// login failed
			res.sendRedirect("/");
		}
		return null;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@HeaderParam("X-Requested-With")
	public List<Repository> repolist() {
		return null;
	}

	@GET
	@Path("/{path}/{project}")
	@Produces(MediaType.APPLICATION_JSON)
	// @HeaderParam("X-Requested-With")
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
	// @HeaderParam("X-Requested-With")
	public String tree(@PathParam("path") String path,
			@PathParam("project") String project,
			@PathParam("branch") String branch) {

		return path + "--" + project + "--" + branch;
	}
}
