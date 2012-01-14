package org.koshinuke.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
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
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.UnmergedPathException;
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
	public List<RepositoryModel> list() throws IOException {
		final List<RepositoryModel> repos = new ArrayList<>();
		java.nio.file.Path dir = this.config.getRepositoryRootDir();
		try (DirectoryStream<java.nio.file.Path> parentStream = java.nio.file.Files
				.newDirectoryStream(dir)) {
			for (java.nio.file.Path parent : parentStream) {
				try (DirectoryStream<java.nio.file.Path> kidsStream = java.nio.file.Files
						.newDirectoryStream(parent)) {
					for (java.nio.file.Path maybeRepo : kidsStream) {
						RepositoryModel repo = this.to(maybeRepo.toFile());
						if (repo != null) {
							repos.add(repo);
						}
					}
				}
			}
		}
		return repos;
	}

	protected RepositoryModel to(File maybeRepo) {
		try {
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			FileRepository repo = builder.setGitDir(maybeRepo)
					.readEnvironment().setMustExist(true).build();
			return new RepositoryModel(this.config.getGitHost(), repo);
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
		}
		return null;
	}

	@POST
	@Path("/dynamic")
	public List<RepositoryModel> init(@Context KoshinukePrincipal p,
			@FormParam("rn") String name, @FormParam("rr") String readme)
			throws Exception {
		// TODO 適切にUIが動作していれば発生しないケースにおけるエラー処理
		if (StringUtils.isEmptyOrNull(name) == false
				&& StringUtils.isEmptyOrNull(readme) == false) {
			String[] ary = name.split("/");
			if (ary.length == 2) {
				java.nio.file.Path repoRoot = this.config
						.getRepositoryRootDir();
				java.nio.file.Path path = repoRoot.resolve(name).normalize();
				if (path.startsWith(repoRoot) && path.equals(repoRoot) == false) {
					File newrepo = path.toFile();
					if (newrepo.exists() == false) {
						this.initRepository(p, readme, newrepo);
					}
				}
			}
		}
		return this.list();
	}

	protected void initRepository(KoshinukePrincipal p, String readme,
			File newrepo) throws MalformedURLException, IOException,
			NoFilepatternException, NoHeadException, NoMessageException,
			UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException, InvalidRemoteException {
		Git.init().setBare(true).setDirectory(newrepo).call();
		File working = pickWorkingDir(this.config.getWorkingDir());
		try {
			Git g = Git.cloneRepository()
					.setURI(newrepo.toURI().toURL().toString())
					.setDirectory(working).call();
			File readmeFile = new File(working, "README");
			Files.write(readme, readmeFile, ReaderWriter.UTF8);
			g.add().addFilepattern(readmeFile.getName()).call();
			PersonIdent commiter = this.config.getSystemIdent();
			PersonIdent author = new PersonIdent(p.getName(), p.getMail(),
					commiter.getWhen(), commiter.getTimeZone());
			g.commit().setMessage("initial commit.").setCommitter(commiter)
					.setAuthor(author).call();
			g.push().call();
		} finally {
			FileUtil.delete(working.getAbsolutePath());
		}
	}

	protected static File pickWorkingDir(java.nio.file.Path root) {
		java.nio.file.Path working = null;
		do {
			working = root.resolve(RandomUtil.nextString());
		} while (java.nio.file.Files.exists(working));
		return working.toFile();
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
