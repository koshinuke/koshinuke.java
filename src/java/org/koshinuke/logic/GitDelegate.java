package org.koshinuke.logic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.iharder.Base64;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectStream;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.StringUtils;
import org.koshinuke._;
import org.koshinuke.conf.Configuration;
import org.koshinuke.jgit.SimpleDiffFormatter;
import org.koshinuke.model.BlameModel;
import org.koshinuke.model.BlobModel;
import org.koshinuke.model.BranchHistoryModel;
import org.koshinuke.model.CommitModel;
import org.koshinuke.model.DiffEntryModel;
import org.koshinuke.model.DiffModel;
import org.koshinuke.model.KoshinukePrincipal;
import org.koshinuke.model.NodeModel;
import org.koshinuke.model.RepositoryModel;
import org.koshinuke.util.GitUtil;
import org.koshinuke.util.IORuntimeException;
import org.koshinuke.util.RandomUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.NullOutputStream;

/**
 * @author taichi
 */
public class GitDelegate {

	static final Logger LOG = Logger.getLogger(GitDelegate.class.getName());

	final Configuration config;

	public GitDelegate(Configuration config) {
		this.config = config;
	}

	public List<RepositoryModel> listRepository() {
		final List<RepositoryModel> repos = new ArrayList<>();
		try {
			Path dir = this.config.getRepositoryRootDir();
			try (DirectoryStream<Path> parentStream = java.nio.file.Files
					.newDirectoryStream(dir)) {
				for (Path parent : parentStream) {
					try (DirectoryStream<Path> kidsStream = java.nio.file.Files
							.newDirectoryStream(parent)) {
						for (Path maybeRepo : kidsStream) {
							RepositoryModel repo = this.to(maybeRepo);
							if (repo != null) {
								repos.add(repo);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
		}
		return repos;
	}

	protected RepositoryModel to(Path maybeRepo) {
		try {
			return GitUtil.handleLocal(maybeRepo,
					new Function<Repository, RepositoryModel>() {
						@Override
						public RepositoryModel apply(Repository repo) {
							return new RepositoryModel(GitDelegate.this.config
									.getGitHost(), repo);
						}
					});
		} catch (IORuntimeException e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
		}
		return null;
	}

	public boolean initRepository(final KoshinukePrincipal p, String name,
			String readme) {
		Path repoRoot = this.config.getRepositoryRootDir();
		Path path = repoRoot.resolve(name).normalize();
		if (path.startsWith(repoRoot) && path.equals(repoRoot) == false) {
			File newrepo = path.toFile();
			if (newrepo.exists() == false) {
				this.init(p, newrepo, readme);
				return true;
			}
		}
		return false;
	}

	protected void init(final KoshinukePrincipal p, final File newrepo,
			final String readme) {
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
								PersonIdent commiter = GitDelegate.this.config
										.getSystemIdent();
								PersonIdent author = GitDelegate.this
										.makeAuthorIdent(p, commiter);
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
			this.delete(working);
		}
	}

	protected void delete(File f) {
		try {
			FileUtils.delete(f, FileUtils.RECURSIVE | FileUtils.SKIP_MISSING);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	protected static File pickWorkingDir(Path root) {
		Path working = null;
		do {
			working = root.resolve(RandomUtil.nextString());
		} while (java.nio.file.Files.exists(working));
		return working.toFile();
	}

	public boolean cloneRepository(final KoshinukePrincipal p, String uri,
			String un, String up) {
		try {
			URIish u = new URIish(uri);
			if (StringUtils.isEmptyOrNull(un) == false
					&& StringUtils.isEmptyOrNull(up) == false) {
				u.setUser(un);
				u.setPass(up);
			}
			// ローカルディレクトリの読み取りは許可しない。
			// TODO support ssh+git
			if (StringUtils.isEmptyOrNull(u.getHost()) == false) {
				final File local = pickWorkingDir(this.config.getWorkingDir());
				try {
					final String humanish = u.getHumanishName();
					GitUtil.handleClone(u.toString(),
							new File(local, humanish), true,
							new Function<Git, _>() {
								@Override
								public _ apply(Git input) {
									GitDelegate.this.copyCloningRepository(p,
											input, humanish);
									return _._;
								}
							});
				} finally {
					this.delete(local);
				}
				return true;
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
		}
		return false;
	}

	protected void copyCloningRepository(final KoshinukePrincipal p, Git input,
			String humanish) {
		Repository repo = input.getRepository();
		File dir = repo.getDirectory();
		// TODO 特定のプロジェクトに紐付けるのが正しいのでは無いか？
		Path userPath = GitDelegate.this.config.getRepositoryRootDir().resolve(
				p.getName());
		Path newone = userPath.resolve(humanish);
		if (java.nio.file.Files.exists(newone)) {
			File f = pickWorkingDir(userPath);
			newone = f.toPath();
		}
		try {
			java.nio.file.Files.copy(dir.toPath(), newone);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	public List<NodeModel> listRepository(String project, String repository,
			final String rev, final int offset, final int limit) {
		return this.handleLocal(project, repository,
				new Function<Repository, List<NodeModel>>() {
					@Override
					public List<NodeModel> apply(Repository repo) {
						return GitDelegate.this.walkRepository(repo, rev,
								offset, limit);
					}
				});
	}

	protected List<NodeModel> walkRepository(final Repository repo, String rev,
			final int offset, final int limit) {
		final WalkingContext context = new WalkingContext(rev);
		final ObjectId oid = this.findRootObject(repo, context);
		if (oid != null) {
			return GitUtil.walk(repo, new Function<RevWalk, List<NodeModel>>() {
				@Override
				public List<NodeModel> apply(RevWalk walk) {
					Map<String, NodeModel> map = GitDelegate.this.walkTree(
							walk, repo, oid, context, offset, limit);
					if (0 < map.size()) {
						return GitDelegate.this.walkCommits(walk, repo, oid,
								context.root, map);
					}
					return Collections.emptyList();
				}
			});
		}
		return Collections.emptyList();
	}

	protected ObjectId findRootObject(Repository repo, WalkingContext context) {
		ObjectId result = null;
		Ref ref = this.findRef(repo, context);
		if (ref == null) {
			String maybeoid = context.rev;
			int i = maybeoid.indexOf('/');
			if (0 < i) {
				maybeoid = maybeoid.substring(0, i);
				context.root = maybeoid;
				context.resource = maybeoid.substring(i + 1);
			}
			if (maybeoid.length() == Constants.OBJECT_ID_STRING_LENGTH) {
				ObjectId oid = ObjectId.fromString(maybeoid);
				try {
					ObjectLoader ol = repo.open(oid);
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
					LOG.log(Level.WARNING, e.getMessage(), e);
				}
			}
		} else {
			result = ref.getObjectId();
		}
		return result;
	}

	protected Ref findRef(Repository repo, WalkingContext context) {
		Ref r = this.findRef(GitUtil.getBranches(repo), context);
		if (r == null) {
			r = this.findRef(repo.getTags(), context);
		}
		return r;
	}

	protected Ref findRef(Map<String, Ref> refs, WalkingContext context) {
		String rev = context.rev;
		for (String s : refs.keySet()) {
			if (rev.startsWith(s)) {
				context.root = s;
				if (s.length() < rev.length()) {
					context.resource = rev.substring(s.length() + 1);
				}
				return refs.get(s);
			}
		}
		return null;
	}

	protected Map<String, NodeModel> walkTree(RevWalk walk, Repository repo,
			ObjectId oid, WalkingContext context, int offset, int limit) {
		Map<String, NodeModel> result = new HashMap<>();
		TreeWalk tw = new TreeWalk(repo);
		tw.setRecursive(false);
		String parentPath = context.resource;
		try {
			tw.reset(walk.parseTree(oid));
			List<WalkingCandidate> candidates = new ArrayList<>();
			WalkingCandidate current = null;
			int depth = 0;
			do {
				while (tw.next()) {
					if (current != null && current.nm != null) {
						current.nm.addChildren();
					}
					if (result.size() < limit) {
						WalkingCandidate cand = current;
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
							candidates.add(new WalkingCandidate(tw
									.getObjectId(depth), tw.getNameString(),
									current, nm));
						}
					}
				}
				if (0 < candidates.size()) {
					WalkingCandidate c = candidates.remove(0);
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

	// TODO リポジトリのログが増えるとコストが高くなりがちなので、遡るコミット数にしきい値を設け、
	// それで解決出来ないものは、再リクエストによって遅延ロードする仕組み。
	protected List<NodeModel> walkCommits(RevWalk walk, Repository repo,
			ObjectId objectId, String root, Map<String, NodeModel> map) {
		List<NodeModel> result = new ArrayList<>(map.size());
		try {
			RevCommit commit = walk.parseCommit(objectId);
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
					this.addResult(result, nm, root);
				}
			}
			TreeFilter tf = PathFilterGroup.create(filters);
			walk.setTreeFilter(tf);
			DiffFormatter diffFmt = this.makeDiffFormatter(repo, tf);

			outer: for (RevCommit rc : walk) {
				final RevTree now = rc.getTree();
				if (0 < rc.getParentCount()) {
					final RevTree pre = rc.getParent(0).getTree();
					for (DiffEntry de : diffFmt.scan(now, pre)) {
						NodeModel nm = this.remove(map, de.getNewPath(),
								de.getOldPath());
						this.setLastCommit(result, rc, nm, root);
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
							this.setLastCommit(result, rc, nm, root);
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

	protected DiffFormatter makeDiffFormatter(Repository repo, TreeFilter tf) {
		// see. org.eclipse.jgit.diff.DiffConfig
		DiffFormatter diffFmt = new DiffFormatter(new NullOutputStream());
		diffFmt.setRepository(repo);
		diffFmt.setPathFilter(tf);
		return diffFmt;
	}

	protected NodeModel remove(Map<String, NodeModel> diffCand, String l,
			String r) {
		NodeModel nm = diffCand.remove(l);
		if (nm == null) {
			nm = diffCand.remove(r);
		}
		return nm;
	}

	protected void setLastCommit(List<NodeModel> result, RevCommit rc,
			NodeModel nm, String root) {
		if (nm != null) {
			nm.setLastCommit(rc);
			this.addResult(result, nm, root);
		}
	}

	protected void addResult(List<NodeModel> result, NodeModel nm, String root) {
		nm.setPath(root + "/" + nm.getPath());
		result.add(nm);
	}

	public BlobModel getBlob(String project, String repository, final String rev) {
		return this.handleLocal(project, repository,
				new Function<Repository, BlobModel>() {
					@Override
					public BlobModel apply(Repository repo) {
						return GitDelegate.this.findBlob(repo, rev);
					}
				});
	}

	protected BlobModel findBlob(final Repository repo, String rev) {
		final WalkingContext context = new WalkingContext(rev);
		final ObjectId oid = this.findRootObject(repo, context);
		if (oid != null) {
			return GitUtil.walk(repo, new Function<RevWalk, BlobModel>() {
				@Override
				public BlobModel apply(RevWalk walk) {
					BlobModel bm = GitDelegate.this.findBlob(walk, repo, oid,
							context);
					GitDelegate.this.walkCommits(walk, repo, oid, context, bm);
					return bm;
				}
			});
		}
		return null;
	}

	protected BlobModel findBlob(RevWalk walk, Repository repo, ObjectId oid,
			WalkingContext context) {
		try {
			final BlobModel bm = new BlobModel();
			this.setContent(repo, walk.parseTree(oid), context.resource,
					new BlobHolder() {
						@Override
						public void setId(ObjectId oid) {
							bm.setObjectId(oid);
						}

						@Override
						public void setContent(String content) {
							bm.setContent(content);
						}
					});
			return bm;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	protected void walkCommits(RevWalk walk, Repository repo, ObjectId oid,
			WalkingContext context, BlobModel bm) {
		try {
			RevCommit commit = walk.parseCommit(oid);
			walk.reset();
			walk.markStart(commit);
			TreeFilter tf = PathFilter.create(context.resource);
			walk.setTreeFilter(tf);
			DiffFormatter diffFmt = this.makeDiffFormatter(repo, tf);
			outer: for (RevCommit rc : walk) {
				final RevTree now = rc.getTree();
				if (0 < rc.getParentCount()) {
					final RevTree pre = rc.getParent(0).getTree();
					for (DiffEntry de : diffFmt.scan(now, pre)) {
						if (context.resource.equals(de.getNewPath())
								|| context.resource.equals(de.getOldPath())) {
							bm.setLastCommit(rc);
							break outer;
						}
					}
				} else {
					TreeWalk tw = new TreeWalk(repo);
					tw.reset(rc.getTree());
					tw.setRecursive(true);
					try {
						while (tw.next()) {
							if (context.resource.equals(tw.getPathString())) {
								bm.setLastCommit(rc);
								break outer;
							}
						}
					} finally {
						tw.release();
					}
				}
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	public BlobModel modifyBlob(final KoshinukePrincipal p, String project,
			String repository, final String rev, final BlobModel input) {
		return this.handleLocal(project, repository,
				new Function<Repository, BlobModel>() {
					@Override
					public BlobModel apply(Repository repo) {
						return GitDelegate.this.modifyBlob(p, repo, rev, input);
					}
				});
	}

	protected BlobModel modifyBlob(final KoshinukePrincipal p,
			final Repository repo, final String rev, final BlobModel input) {
		final WalkingContext context = new WalkingContext(rev);
		final ObjectId oid = this.findRootObject(repo, context);
		if (oid != null) {
			return GitUtil.walk(repo, new Function<RevWalk, BlobModel>() {
				@Override
				public BlobModel apply(RevWalk walk) {
					return GitDelegate.this.modifyBlob(p, walk, repo, oid,
							context, input);
				}
			});
		}
		return null;
	}

	protected BlobModel modifyBlob(KoshinukePrincipal p, RevWalk walk,
			Repository repo, ObjectId oid, WalkingContext context,
			BlobModel input) {
		TreeWalk tw = new TreeWalk(repo);
		tw.setRecursive(true);
		try {
			RevObject ro = walk.parseAny(oid);
			if (ro.getType() == Constants.OBJ_COMMIT) {
				tw.reset(walk.parseTree(oid));
				tw.setFilter(PathFilter.create(context.resource));
				if (tw.next()) {
					ObjectId o = tw.getObjectId(0);
					if (o.equals(input.getObjectId())) {
						RevCommit commit = walk.parseCommit(oid);
						return this.modifyResource(p, repo, commit, context,
								input);
					}
				}
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		} finally {
			tw.release();
		}
		return null;
	}

	protected BlobModel modifyResource(final KoshinukePrincipal p,
			Repository repo, final RevCommit commit,
			final WalkingContext context, final BlobModel input) {
		final File working = pickWorkingDir(this.config.getWorkingDir());
		try {
			return GitUtil.handleClone(repo.getDirectory().toURI(), working,
					new Function<Git, BlobModel>() {
						@Override
						public BlobModel apply(Git g) {
							try {
								boolean create = g.getRepository().getRef(
										Constants.R_HEADS + context.root) == null;
								g.checkout().setCreateBranch(create)
										.setStartPoint(commit)
										.setName(context.root).call();
								File file = new File(working, context.resource);
								Files.write(input.getContent(), file,
										Charsets.UTF_8);
								g.add().addFilepattern(context.resource).call();
								PersonIdent commiter = GitDelegate.this.config
										.getSystemIdent();
								PersonIdent author = GitDelegate.this
										.makeAuthorIdent(p, commiter);
								RevCommit commit = g.commit()
										.setMessage(input.getMessage())
										.setCommitter(commiter)
										.setAuthor(author).call();
								g.push().call();
								input.setLastCommit(commit);
								return input;
							} catch (GitAPIException e) {
								throw new IllegalStateException(e);
							} catch (IOException e) {
								throw new IORuntimeException(e);
							}
						}
					});
		} finally {
			this.delete(working);
		}
	}

	protected PersonIdent makeAuthorIdent(final KoshinukePrincipal p,
			PersonIdent commiter) {
		PersonIdent author = new PersonIdent(p.getName(), p.getMail(),
				commiter.getWhen(), commiter.getTimeZone());
		return author;
	}

	public List<BranchHistoryModel> getHistories(String project,
			String repository) {
		return this.handleLocal(project, repository,
				new Function<Repository, List<BranchHistoryModel>>() {
					@Override
					public List<BranchHistoryModel> apply(Repository repo) {
						return GitDelegate.this.findBranchHistories(repo);
					}
				});
	}

	protected List<BranchHistoryModel> findBranchHistories(final Repository repo) {
		List<BranchHistoryModel> result = new ArrayList<>();
		Map<String, Ref> branches = GitUtil.getBranches(repo);
		for (String s : branches.keySet()) {
			final BranchHistoryModel model = new BranchHistoryModel(s);
			final Ref ref = branches.get(s);
			List<long[]> activities = GitUtil.walk(repo,
					new Function<RevWalk, List<long[]>>() {
						@Override
						public List<long[]> apply(RevWalk walk) {
							RevCommit commit = GitDelegate.this.setLastCommit(
									walk, ref, model);
							return GitDelegate.this.parseActivities(walk, repo,
									commit);
						}
					});
			model.setActivities(activities);
			result.add(model);
		}
		return result;
	}

	protected RevCommit setLastCommit(RevWalk walk, Ref ref,
			BranchHistoryModel model) {
		try {
			ObjectId oid = ref.getObjectId();
			RevCommit commit = walk.parseCommit(oid);
			model.setLastCommit(commit);
			return commit;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	protected List<long[]> parseActivities(RevWalk walk, Repository repo,
			RevCommit begin) {
		List<long[]> result = new ArrayList<>(30);
		try {
			walk.reset();
			walk.markStart(begin);
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			class TimeContext {
				long time;
				int count;
			}
			List<TimeContext> list = new ArrayList<>();
			for (int i = 0; i < 30; i++) {
				TimeContext tc = new TimeContext();
				tc.time = calendar.getTimeInMillis();
				list.add(tc);
				calendar.add(Calendar.DATE, -1);
			}

			for (RevCommit cmt : walk) {
				PersonIdent author = cmt.getAuthorIdent();
				long time = author.getWhen().getTime();
				for (TimeContext tc : list) {
					if (tc.time < time) {
						tc.count++;
						break;
					}
				}
			}
			Collections.reverse(list);
			for (TimeContext tc : list) {
				long[] ary = { tc.time / 1000L, tc.count };
				result.add(ary);
			}
			return result;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	protected <T> T handleLocal(String project, String repository,
			Function<Repository, T> handler) {
		Path path = this.config.getRepositoryRootDir().resolve(project)
				.resolve(repository);
		if (java.nio.file.Files.exists(path)) {
			try {
				return GitUtil.handleLocal(path, handler);
			} catch (IORuntimeException e) {
				LOG.log(Level.WARNING, e.getMessage(), e);
			}
		}
		return null;
	}

	public List<CommitModel> getCommits(String project, String repository,
			final String rev, final String offset, final int limit) {
		return this.handleLocal(project, repository,
				new Function<Repository, List<CommitModel>>() {
					@Override
					public List<CommitModel> apply(Repository repo) {
						return GitDelegate.this.parseCommits(repo, rev, offset,
								limit);
					}
				});
	}

	protected List<CommitModel> parseCommits(final Repository repo, String rev,
			final String offset, final int limit) {
		final WalkingContext context = new WalkingContext(rev);
		final ObjectId oid = this.findRootObject(repo, context);
		if (oid != null) {
			return GitUtil.walk(repo,
					new Function<RevWalk, List<CommitModel>>() {
						@Override
						public List<CommitModel> apply(RevWalk walk) {
							return GitDelegate.this.parseCommits(walk, repo,
									oid, context, offset, limit);
						}
					});
		}

		return Collections.emptyList();
	}

	protected List<CommitModel> parseCommits(RevWalk walk, Repository repo,
			ObjectId oid, WalkingContext context, String offset, int limit) {
		try {
			List<CommitModel> result = new ArrayList<>();
			RevCommit begin = null;
			if (StringUtils.isEmptyOrNull(offset) == false
					&& offset.length() == Constants.OBJECT_ID_STRING_LENGTH) {
				ObjectId offsetId = ObjectId.fromString(offset);
				RevCommit rc = walk.parseCommit(offsetId);
				if (0 < rc.getParentCount()) {
					begin = rc.getParent(0);
				}
			}
			if (begin == null) {
				begin = walk.parseCommit(oid);
			}
			walk.reset();
			walk.markStart(begin);
			if (StringUtils.isEmptyOrNull(context.resource) == false) {
				walk.setTreeFilter(AndTreeFilter.create(
						PathFilter.create(context.resource),
						TreeFilter.ANY_DIFF));
			}
			for (RevCommit rc : walk) {
				if (limit-- < 1) {
					break;
				}
				result.add(new CommitModel(rc));
			}
			return result;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	public DiffModel getDiff(String project, String repository,
			final String commitid) {
		return this.handleLocal(project, repository,
				new Function<Repository, DiffModel>() {
					@Override
					public DiffModel apply(final Repository repo) {
						return GitUtil.walk(repo,
								new Function<RevWalk, DiffModel>() {
									@Override
									public DiffModel apply(RevWalk walk) {
										return GitDelegate.this.getDiff(walk,
												repo, commitid);
									}
								});
					}
				});
	}

	protected DiffModel getDiff(RevWalk walk, Repository repo, String commitid) {
		try {
			ObjectId oid = repo.resolve(commitid);
			RevCommit current = walk.parseCommit(oid);
			DiffModel result = new DiffModel(current);
			result.setLastCommit(current);
			if (0 < current.getParentCount()) {
				ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
				DiffFormatter fmt = new SimpleDiffFormatter(out);
				fmt.setRepository(repo);
				// TODO whitespaceの扱いを設定できる様にする？
				fmt.setDiffComparator(RawTextComparator.WS_IGNORE_CHANGE);
				fmt.setDetectRenames(true);
				// TODO git note を、どう扱うか考える。
				List<DiffEntryModel> list = new ArrayList<>();
				RevTree oldTree = walk.parseCommit(current.getParent(0))
						.getTree();
				RevTree newTree = current.getTree();
				for (DiffEntry de : fmt.scan(oldTree, newTree)) {
					DiffEntryModel dm = new DiffEntryModel(de);
					fmt.format(de);
					fmt.flush();
					dm.setPatch(out.toString(Charsets.UTF_8.name()));
					out.reset();
					this.setContent(repo, oldTree, newTree, de, dm);
					list.add(dm);
				}
				result.setDiff(list);
			}
			return result;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	protected void setContent(Repository repo, RevTree oldTree,
			RevTree newTree, DiffEntry de, final DiffEntryModel dm)
			throws IOException {
		class H implements BlobHolder {
			@Override
			public void setId(ObjectId oid) {
			}

			@Override
			public void setContent(String content) {
			}
		}

		this.setContent(repo, oldTree, de.getOldPath(), new H() {
			@Override
			public void setContent(String content) {
				dm.setOldContent(content);
			}
		});
		this.setContent(repo, newTree, de.getNewPath(), new H() {
			@Override
			public void setContent(String content) {
				dm.setNewContent(content);
			}
		});
	}

	interface BlobHolder {
		void setId(ObjectId oid);

		void setContent(String content);
	}

	protected void setContent(Repository repo, RevTree tree, String path,
			BlobHolder holder) throws IOException {
		TreeWalk tw = new TreeWalk(repo);
		try {
			tw.setRecursive(true);
			tw.reset(tree);
			tw.setFilter(PathFilter.create(path));
			if (tw.next()) {
				ObjectId o = tw.getObjectId(0);
				ObjectLoader ol = tw.getObjectReader().open(o,
						Constants.OBJ_BLOB);
				try (ObjectStream in = ol.openStream()) {
					// TODO コンテンツがデカ過ぎる場合、メモリに全部展開してしまうのは良くないので、
					// クライアント側に対してリソースが大きすぎる事を通知した上で、再リクエストしてもらう仕組みを作りこむ。
					byte[] bytes = ByteStreams.toByteArray(in);
					StringBuilder stb = this.toDataScheme(path);
					if (stb == null) {
						// TODO from config?
						// see. com.ibm.icu.text.CharsetDetector
						// UTF-8以外のモノが混ざる様ならコンテンツの文字エンコーディングをここでUTF-8に変換する必要がある。
						holder.setContent(new String(bytes, Charsets.UTF_8));
					} else {
						stb.append(Base64.encodeBytes(bytes));
						holder.setContent(stb.toString());
					}
					holder.setId(o);
				}
			}
		} finally {
			tw.release();
		}
	}

	static final Pattern isImages = Pattern.compile(
			"\\.(jpe?g|gif|png|ico|bmp)$", Pattern.CASE_INSENSITIVE);

	// see. http://tools.ietf.org/html/rfc2397
	protected StringBuilder toDataScheme(String path) {
		Matcher m = isImages.matcher(path);
		if (m.find()) {
			StringBuilder stb = new StringBuilder();
			stb.append("data:image/");
			stb.append(m.group(1));
			stb.append(";base64,");
			return stb;
		}
		return null;
	}

	public List<BlameModel> getBlame(String project, String repository,
			final String rev) {
		return this.handleLocal(project, repository,
				new Function<Repository, List<BlameModel>>() {
					@Override
					public List<BlameModel> apply(Repository repo) {
						return GitDelegate.this.parseBlame(repo, rev);
					}
				});
	}

	protected List<BlameModel> parseBlame(Repository repo, String rev) {
		final WalkingContext context = new WalkingContext(rev);
		final ObjectId oid = this.findRootObject(repo, context);
		if (oid != null) {
			Git g = new Git(repo);
			BlameResult br = g.blame().setFilePath(context.resource)
					.setStartCommit(oid).setFollowFileRenames(true).call();
			RawText rt = br.getResultContents();
			List<BlameModel> list = new ArrayList<>();
			for (int i = 0, l = rt.size(); i < l; i++) {
				RevCommit rc = br.getSourceCommit(i);
				BlameModel bm = new BlameModel(rc);
				bm.setContent(rt.getString(i));
				list.add(bm);
			}
			return list;
		}
		return null;
	}

}
