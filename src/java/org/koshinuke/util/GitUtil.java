package org.koshinuke.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.storage.file.WindowCache;
import org.eclipse.jgit.storage.file.WindowCacheConfig;

import com.google.common.base.Function;

/**
 * @author taichi
 */
public class GitUtil {

	public static Repository local(Path path) {
		try {
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			return builder.setGitDir(path.toFile()).readEnvironment()
					.setMustExist(true).build();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	public static <T> T handleLocal(Path root, String project,
			String repository, Function<Repository, T> handler) {
		Path path = root.resolve(Paths.get(project, repository));
		if (java.nio.file.Files.exists(path)) {
			return GitUtil.handleLocal(path, handler);
		}
		return null;
	}

	public static <R> R handleLocal(Path path, Function<Repository, R> fn) {
		return handle(local(path), fn);
	}

	public static <R> R handleClone(URI uri, File local, Function<Git, R> fn) {
		return handleClone(uri, local, false, fn);
	}

	public static <R> R handleClone(URI uri, File local, boolean bare,
			Function<Git, R> fn) {
		return handleClone(uri.toString(), local, bare, fn);
	}

	public static <R> R handleClone(String uri, File local, boolean bare,
			Function<Git, R> fn) {
		try {
			Git g = Git.cloneRepository().setURI(uri).setBare(bare)
					.setDirectory(local).call();
			return handle(g, fn);
		} catch (GitAPIException e) {
			throw new IllegalStateException(e);
		}
	}

	public static <R> R handle(Repository repo, Function<Repository, R> fn) {
		try {
			return fn.apply(repo);
		} finally {
			close(repo);
		}
	}

	public static <R> R handle(Git g, Function<Git, R> handler) {
		try {
			return handler.apply(g);
		} finally {
			close(g);
		}
	}

	public static void close(Repository repo) {
		if (repo != null) {
			repo.close();
		}
	}

	public static void clearCache() {
		WindowCache.reconfigure(new WindowCacheConfig());
	}

	public static void close(Git git) {
		if (git != null) {
			close(git.getRepository());
		}
	}

	public static <T> T walk(Repository repo, Function<RevWalk, T> handler) {
		RevWalk walk = new RevWalk(repo);
		try {
			return handler.apply(walk);
		} finally {
			walk.dispose();
		}
	}

	public static Map<String, Ref> getBranches(Repository repo) {
		try {
			return repo.getRefDatabase().getRefs(Constants.R_HEADS);
		} catch (IOException e) {
			return new HashMap<String, Ref>();
		}
	}
}
