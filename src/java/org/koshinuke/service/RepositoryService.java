package org.koshinuke.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.StringUtils;
import org.koshinuke._;
import org.koshinuke.conf.Configuration;
import org.koshinuke.git.GitHandler;
import org.koshinuke.git.GitUtil;
import org.koshinuke.git.RepositoryHandler;
import org.koshinuke.model.KoshinukePrincipal;
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
@Path("/dynamic")
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryService {

	static final Logger LOG = Logger.getLogger(RepositoryService.class
			.getName());

	@Context
	Configuration config;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable forwardToIndex(@Context HttpServletResponse res) {
		ServletUtil.redirect(res, "/");
		return null;
	}

	@GET
	public List<RepositoryModel> list() throws IOException {
		final List<RepositoryModel> repos = new ArrayList<>();
		java.nio.file.Path dir = this.config.getRepositoryRootDir();
		try (DirectoryStream<java.nio.file.Path> parentStream = java.nio.file.Files
				.newDirectoryStream(dir)) {
			for (java.nio.file.Path parent : parentStream) {
				try (DirectoryStream<java.nio.file.Path> kidsStream = java.nio.file.Files
						.newDirectoryStream(parent)) {
					for (java.nio.file.Path maybeRepo : kidsStream) {
						RepositoryModel repo = this.to(maybeRepo);
						if (repo != null) {
							repos.add(repo);
						}
					}
				}
			}
		}
		return repos;
	}

	protected RepositoryModel to(java.nio.file.Path maybeRepo) {
		try {
			return GitUtil.handleLocal(maybeRepo,
					new RepositoryHandler<RepositoryModel>() {
						@Override
						public RepositoryModel handle(Repository repo)
								throws Exception {
							return new RepositoryModel(
									RepositoryService.this.config.getGitHost(),
									repo);
						}
					});
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
		}
		return null;
	}

	@POST
	public Response init(@Context KoshinukePrincipal p,
			@FormParam("rn") String name, @FormParam("rr") String readme)
			throws Exception {
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
						return Response.status(HttpServletResponse.SC_CREATED)
								.entity(this.list()).build();
					}
				}
			}
		}
		return Response.status(ServletUtil.SC_UNPROCESSABLE_ENTITY).build();
	}

	protected void initRepository(final KoshinukePrincipal p,
			final String readme, final File newrepo) throws Exception {
		Git initialized = null;
		final File working = pickWorkingDir(this.config.getWorkingDir());
		try {
			initialized = Git.init().setBare(true).setDirectory(newrepo).call();
			GitUtil.handleClone(newrepo.toURI(), working, new GitHandler<_>() {
				@Override
				public _ handle(Git g) throws Exception {
					File readmeFile = new File(working, "README");
					Files.write(readme, readmeFile, ReaderWriter.UTF8);
					g.add().addFilepattern(readmeFile.getName()).call();
					PersonIdent commiter = RepositoryService.this.config
							.getSystemIdent();
					PersonIdent author = new PersonIdent(p.getName(), p
							.getMail(), commiter.getWhen(), commiter
							.getTimeZone());
					g.commit().setMessage("initial commit.")
							.setCommitter(commiter).setAuthor(author).call();
					g.push().call();
					return _._;
				}
			});
		} finally {
			GitUtil.close(initialized);
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

	static final String REV_PART = "{rev: ([a-zA-Z0-9/-_\\+\\*\\.]|%[0-9a-fA-F]{2})+}";

	@GET
	@Path("/{project}/{repository}/tree/" + REV_PART)
	public Response tree(@PathParam("project") String project,
			@PathParam("repository") String repository,
			@PathParam("rev") String rev,
			// TODO とりあえず定義。使うのは後で。
			@QueryParam("offset") String offset,
			@QueryParam("limit") String limit) throws Exception {
		java.nio.file.Path path = this.config.getRepositoryRootDir()
				.resolve(project).resolve(repository);

		if (java.nio.file.Files.exists(path)) {
			Repository repo = GitUtil.to(path);
			return Response.ok("hogehoge").build();
		}
		return Response.status(ServletUtil.SC_UNPROCESSABLE_ENTITY).build();
	}

}
