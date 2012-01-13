package org.koshinuke.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.StringUtils;
import org.koshinuke.conf.Configuration;
import org.koshinuke.model.Auth;
import org.koshinuke.model.KoshinukePrincipal;
import org.koshinuke.model.Repository;
import org.koshinuke.model.RepositoryModel;
import org.koshinuke.util.FileUtil;
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

	static final Logger LOG = Logger.getLogger(RepositoryService.class
			.getName());

	@Context
	Configuration config;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index(@Context KoshinukePrincipal p,
			@Context HttpServletRequest req, @Context HttpServletResponse res) {
		if (p == null) {
			ServletUtil.redirect(res, "/login");
			return null;
		}
		return Auth.of("/repos", req.getSession(), p);
	}

	@GET
	@Path("/dynamic")
	@Produces(MediaType.TEXT_HTML)
	public Viewable forwardToIndex(@Context HttpServletResponse res) {
		ServletUtil.redirect(res, "/");
		return null;
	}

	@GET
	@Path("/dynamic")
	public List<RepositoryModel> repolist() {
		List<RepositoryModel> repos = new ArrayList<>();
		File dir = this.config.getRepositoryRootDir();
		for (File parent : dir.listFiles()) {
			for (File maybeRepo : parent.listFiles()) {
				RepositoryModel repo = this.to(maybeRepo);
				if (repo != null) {
					repos.add(repo);
				}
			}
		}
		return repos;
	}

	protected RepositoryModel to(File maybeRepo) {
		try {
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			FileRepository repo = builder.setGitDir(maybeRepo)
					.readEnvironment().build();
			return new RepositoryModel(this.config.getGitHost(), repo);
		} catch (IOException e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
		}
		return null;
	}

	@POST
	@Path("/dynamic")
	public List<RepositoryModel> init(@Context KoshinukePrincipal p,
			@FormParam("rn") String name, @FormParam("rrn") String readme)
			throws IOException, GitAPIException {
		if (StringUtils.isEmptyOrNull(name) == false) {
			String[] ary = name.split("/");
			if (ary.length == 2) {
				java.nio.file.Path repoRoot = this.config
						.getRepositoryRootDir().toPath();
				java.nio.file.Path path = repoRoot.resolve(name).normalize();
				if (path.startsWith(repoRoot) && path.equals(repoRoot) == false) {
					File newrepo = path.toFile();
					if (newrepo.exists() == false) {
						Git.init().setBare(true).setDirectory(newrepo).call();
						if (StringUtils.isEmptyOrNull(readme) == false) {
							File working = pickWorkingDir(this.config
									.getWorkingDir());
							Git g = Git.cloneRepository().setBranch("HEAD")
									.setURI(newrepo.toURI().toURL().toString())
									.setDirectory(working).call();
							File readmeFile = new File(working, "README");
							Files.write(readme, readmeFile, ReaderWriter.UTF8);
							g.add().addFilepattern(readmeFile.getName()).call();

							PersonIdent commiter = this.config.getSystemIdent();
							PersonIdent author = new PersonIdent(p.getName(),
									p.getMail(), commiter.getWhen(),
									commiter.getTimeZone());
							g.commit().setMessage("initial commit.")
									.setCommitter(commiter).setAuthor(author)
									.call();
							g.push().call();
							FileUtil.delete(working.getAbsolutePath());
						}
					}
				}
			}
		}
		return this.repolist();
	}

	protected static File pickWorkingDir(File root) {
		File working = null;
		do {
			working = new File(root, RandomUtil.nextString());
		} while (working.exists());
		return working;
	}

	@GET
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
	@Path("/dynamic/{project}/{repository}/tree/{branch}")
	public String tree(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("branch") String branch) {

		return project + "--" + repository + "--" + branch;
	}
}
