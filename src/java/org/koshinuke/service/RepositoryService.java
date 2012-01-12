package org.koshinuke.service;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.StringUtils;
import org.koshinuke.conf.Configuration;
import org.koshinuke.filter.AuthenticationFilter;
import org.koshinuke.model.Auth;
import org.koshinuke.model.Repository;
import org.koshinuke.util.RandomUtil;
import org.koshinuke.util.ServletUtil;

import com.google.common.io.Files;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.spi.resource.Singleton;

/**
 * @author taichi
 */
@Singleton
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryService {

	@Context
	Configuration config;

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
		// TODO
		return null;
	}

	@POST
	@HeaderParam("X-Requested-With")
	@Path("/dynamic")
	public List<Repository> init(@Context HttpServletRequest request,
			@FormParam("rn") String name, @FormParam("rrn") String readme)
			throws IOException, GitAPIException {
		if (StringUtils.isEmptyOrNull(name) == false) {
			String[] ary = name.split("/");
			if (ary.length == 2) {
				File newrepo = new File(config.getRepositoryRootDir(), name);
				if (newrepo.exists() == false) {
					Git.init().setBare(true).setDirectory(newrepo).call();
					if (StringUtils.isEmptyOrNull(readme) == false) {
						File working = pickWorkingDir(config.getWorkingDir());
						Git g = Git.cloneRepository().setBranch("HEAD")
								.setDirectory(working).call();
						File readmeFile = new File(working, "README");
						Files.write(readme, readmeFile, ReaderWriter.UTF8);
						g.add().addFilepattern(readmeFile.getName()).call();
						Principal p = AuthenticationFilter
								.getUserPrincipal(request);
						// TODO ユーザのメールアドレス
						g.commit().setMessage("initial commit.")
								.setCommitter(config.getSystemIdent())
								.setAuthor(p.getName(), "").call();
						g.push().call();
						working.delete();
					}
				}
			}
		}
		return repolist();
	}

	protected static File pickWorkingDir(File root) {
		File working = null;
		do {
			working = new File(root, RandomUtil.nextString());
		} while (working.exists());
		return working;
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
