package org.koshinuke.git;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * @author taichi
 */
public class GitUtil {

	public static Repository to(Path path) throws IOException {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		return builder.setGitDir(path.toFile()).readEnvironment()
				.setMustExist(true).build();
	}

	public static <R> R handleLocal(Path path, RepositoryHandler<R> handler)
			throws Exception {
		return handle(to(path), handler);
	}

	public static <R> R handleClone(URI uri, File local, GitHandler<R> handler)
			throws Exception {
		return handleClone(uri, local, false, handler);
	}

	public static <R> R handleClone(URI uri, File local, boolean bare,
			GitHandler<R> handler) throws Exception {
		Git g = Git.cloneRepository().setURI(uri.toString()).setBare(bare)
				.setDirectory(local).call();
		return handle(g, handler);
	}

	public static <R> R handle(Repository repo, RepositoryHandler<R> handler)
			throws Exception {
		try {
			return handler.handle(repo);
		} finally {
			close(repo);
		}
	}

	public static <R> R handle(Git g, GitHandler<R> handler) throws Exception {
		try {
			return handler.handle(g);
		} finally {
			close(g);
		}
	}

	public static void close(Repository repo) {
		if (repo != null) {
			repo.close();
		}
	}

	public static void close(Git git) {
		if (git != null) {
			close(git.getRepository());
		}
	}

	public static <T> T walk(Repository repo, RevWalkHandler<T> handler)
			throws Exception {
		RevWalk walk = new RevWalk(repo);
		try {
			return handler.handle(walk);
		} finally {
			walk.dispose();
		}
	}

	public static Map<String, Ref> getBranches(Repository repo)
			throws IOException {
		return repo.getRefDatabase().getRefs(Constants.R_HEADS);
	}
}
