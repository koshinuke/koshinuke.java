package org.koshinuke.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.StringUtils;
import org.koshinuke._;
import org.koshinuke.conf.Configuration;
import org.koshinuke.model.KoshinukePrincipal;
import org.koshinuke.model.NodeModel;
import org.koshinuke.model.RepositoryModel;
import org.koshinuke.util.FileUtil;
import org.koshinuke.util.GitUtil;
import org.koshinuke.util.IORuntimeException;
import org.koshinuke.util.RandomUtil;
import org.koshinuke.util.ServletUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.io.Files;
import com.google.common.io.NullOutputStream;
import com.sun.jersey.api.view.Viewable;
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
					new Function<Repository, RepositoryModel>() {
						@Override
						public RepositoryModel apply(Repository repo) {
							return new RepositoryModel(
									RepositoryService.this.config.getGitHost(),
									repo);
						}
					});
		} catch (IORuntimeException e) {
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
			final String readme, final File newrepo) {
		Git initialized = null;
		final File working = pickWorkingDir(this.config.getWorkingDir());
		try {
			initialized = Git.init().setBare(true).setDirectory(newrepo).call();
			GitUtil.handleClone(newrepo.toURI(), working,
					new Function<Git, _>() {
						@Override
						public _ apply(Git g) {
							try {
								// TODO config or input ?
								File readmeFile = new File(working, "README");
								Files.write(readme, readmeFile, Charsets.UTF_8);
								g.add().addFilepattern(readmeFile.getName())
										.call();
								PersonIdent commiter = RepositoryService.this.config
										.getSystemIdent();
								PersonIdent author = new PersonIdent(p
										.getName(), p.getMail(), commiter
										.getWhen(), commiter.getTimeZone());
								// TODO config or input ?
								g.commit().setMessage("initial commit.")
										.setCommitter(commiter)
										.setAuthor(author).call();
								g.push().call();
							} catch (IOException e) {
								throw new IORuntimeException(e);
							} catch (GitAPIException e) {
								throw new IllegalStateException(e);
							}
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
			final @PathParam("rev") String rev,
			@QueryParam("offset") final String offset,
			// TODO config ?
			@QueryParam("limit") final String limit) {
		java.nio.file.Path path = this.config.getRepositoryRootDir()
				.resolve(project).resolve(repository);

		if (java.nio.file.Files.exists(path)) {
			List<NodeModel> list = GitUtil.handleLocal(path,
					new Function<Repository, List<NodeModel>>() {
						@Override
						public List<NodeModel> apply(Repository repo) {
							return RepositoryService.this.walkRepository(repo,
									rev, to(offset, 0), to(limit, 512));
						}
					});
			return Response.ok(list).build();
		}
		return Response.status(ServletUtil.SC_UNPROCESSABLE_ENTITY).build();
	}

	protected static Pattern isNumeric = Pattern.compile("[0-9]+");

	protected static int to(String s, int dv) {
		if (s != null && isNumeric.matcher(s).matches()) {
			return Integer.parseInt(s);
		}
		return dv;
	}

	protected List<NodeModel> walkRepository(final Repository repo, String rev,
			final int offset, final int limit) {
		try {
			final String[] context = { rev, "" };
			final ObjectId oid = this.findObject(repo, context);
			if (oid != null) {
				return GitUtil.walk(repo,
						new Function<RevWalk, List<NodeModel>>() {
							@Override
							public List<NodeModel> apply(RevWalk walk) {
								Map<String, NodeModel> map = RepositoryService.this
										.walkTree(walk, repo, oid, context,
												offset, limit);
								if (0 < map.size()) {
									return RepositoryService.this.walkCommits(
											walk, repo, oid, map);
								}
								return Collections.emptyList();
							}
						});
			}
		} catch (IORuntimeException e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
		}
		return Collections.emptyList();
	}

	protected ObjectId findObject(Repository repo, String[] context) {
		ObjectId result = null;
		Ref ref = this.findRef(repo, context);
		if (ref == null) {
			String maybeoid = context[0];
			int i = maybeoid.indexOf('/');
			if (0 < i) {
				context[1] = maybeoid.substring(i + 1);
				maybeoid = maybeoid.substring(0, i);
			}
			if (maybeoid.length() == Constants.OBJECT_ID_STRING_LENGTH) {
				ObjectId oid = ObjectId.fromString(maybeoid);
				ObjectLoader ol = null;
				try {
					ol = repo.open(oid);
					switch (ol.getType()) {
					case Constants.OBJ_COMMIT:
					case Constants.OBJ_TAG:
						result = oid;
						break;
					default:
						break;
					}
				} catch (IOException e) {
					// do nothing.
				}
			}
		} else {
			result = ref.getObjectId();
		}
		return result;
	}

	protected Ref findRef(Repository repo, String[] context) {
		Ref r = this.findRef(GitUtil.getBranches(repo), context);
		if (r == null) {
			r = this.findRef(repo.getTags(), context);
		}
		return r;
	}

	protected Ref findRef(Map<String, Ref> refs, String[] context) {
		String rev = context[0];
		for (String s : refs.keySet()) {
			if (rev.startsWith(s)) {
				if (s.length() < rev.length()) {
					context[1] = rev.substring(s.length() + 1);
				}
				return refs.get(s);
			}
		}
		return null;
	}

	class Candidate {
		final ObjectId oid;
		final String name;
		final Candidate parent;
		final NodeModel nm;

		Candidate(ObjectId oid, String name, Candidate parent, NodeModel nm) {
			this.oid = oid;
			this.name = name;
			this.parent = parent;
			this.nm = nm;
		}
	}

	protected Map<String, NodeModel> walkTree(RevWalk walk, Repository repo,
			ObjectId oid, String[] context, int offset, int limit) {
		Map<String, NodeModel> result = new HashMap<>();
		TreeWalk tw = new TreeWalk(repo);
		tw.setRecursive(false);
		String parentPath = context[1];
		try {
			tw.reset(walk.parseTree(oid));
			List<Candidate> candidates = new ArrayList<>();
			Candidate current = null;
			int depth = 0;
			do {
				while (tw.next()) {
					if (current != null && current.nm != null) {
						current.nm.addChildren();
					}
					if (result.size() < limit) {
						Candidate cand = current;
						List<String> names = new ArrayList<>();
						String name = tw.getNameString();
						names.add(name);
						while (cand != null) {
							names.add(cand.name);
							cand = cand.parent;
						}
						Collections.reverse(names);
						String path = this.toPath(names);
						NodeModel nm = this.makeModel(walk, path, name,
								tw.getObjectId(depth));

						boolean startsWith = parentPath.isEmpty()
								|| parentPath.length() < path.length()
								&& path.startsWith(parentPath);
						if ((offset < 1 || 0 < offset--) && startsWith) {
							result.put(path, nm);
						}
						if (tw.isSubtree()
								&& (startsWith || parentPath.startsWith(path))) {
							candidates.add(new Candidate(tw.getObjectId(depth),
									tw.getNameString(), current, nm));
						}
					}
				}
				if (0 < candidates.size()) {
					Candidate c = candidates.remove(0);
					tw.addTree(c.oid);
					depth++;
					current = c;
				} else {
					current = null;
				}
			} while (current != null);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		} finally {
			tw.release();
		}
		return result;
	}

	protected String toPath(List<String> path) {
		StringBuilder stb = new StringBuilder();
		for (Iterator<String> i = path.iterator(); i.hasNext();) {
			String s = i.next();
			stb.append(s);
			if (i.hasNext()) {
				stb.append('/');
			}
		}
		return stb.toString();
	}

	protected NodeModel makeModel(RevWalk walk, String path, String name,
			ObjectId oid) {
		try {
			NodeModel nm = new NodeModel(path, name);
			nm.setObject(walk.parseAny(oid));
			return nm;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	protected List<NodeModel> walkCommits(RevWalk walk, Repository repo,
			ObjectId objectId, Map<String, NodeModel> map) {
		List<NodeModel> result = new ArrayList<>(map.size());
		try {
			RevObject ro = walk.parseAny(objectId);
			RevCommit commit = null;
			if (ro.getType() == Constants.OBJ_TAG) {
				RevTag tag = walk.lookupTag(ro);
				commit = walk.lookupCommit(tag.getObject());
			} else if (ro.getType() == Constants.OBJ_COMMIT) {
				commit = walk.lookupCommit(objectId);
			} else {
				throw new IllegalStateException();
			}
			walk.reset();
			walk.markStart(commit);

			List<PathFilter> filters = new ArrayList<>();
			for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
				String path = i.next();
				NodeModel nm = map.get(path);
				if (nm.getObject().getType() == Constants.OBJ_BLOB) {
					filters.add(PathFilter.create(path));
				} else {
					i.remove();
					result.add(nm);
				}
			}
			TreeFilter tf = PathFilterGroup.create(filters);
			walk.setTreeFilter(tf);
			// see. org.eclipse.jgit.diff.DiffConfig
			DiffFormatter diffFmt = new DiffFormatter(new NullOutputStream());
			diffFmt.setRepository(repo);
			diffFmt.setPathFilter(tf);

			outer: for (RevCommit rc : walk) {
				final RevTree now = rc.getTree();
				if (0 < rc.getParentCount()) {
					final RevTree pre = rc.getParent(0).getTree();
					for (DiffEntry de : diffFmt.scan(now, pre)) {
						NodeModel nm = this.remove(map, de.getNewPath(),
								de.getOldPath());
						this.setLastCommit(result, rc, nm);
						if (map.isEmpty()) {
							break outer;
						}
					}
				} else {
					TreeWalk tw = new TreeWalk(repo);
					tw.reset(rc.getTree());
					tw.setRecursive(true);
					try {
						while (tw.next()) {
							String path = tw.getPathString();
							NodeModel nm = map.remove(path);
							this.setLastCommit(result, rc, nm);
							if (map.isEmpty()) {
								break outer;
							}
						}
					} finally {
						tw.release();
					}
				}
			}

			return result;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	protected void setLastCommit(List<NodeModel> result, RevCommit rc,
			NodeModel nm) {
		if (nm != null) {
			nm.setLastCommit(rc);
			result.add(nm);
		}
	}

	protected NodeModel remove(Map<String, NodeModel> diffCand, String l,
			String r) {
		NodeModel nm = diffCand.remove(l);
		if (nm == null) {
			nm = diffCand.remove(r);
		}
		return nm;
	}
}
