package org.koshinuke.git;

import org.eclipse.jgit.lib.Repository;

/**
 * @author taichi
 * @param <R>
 */
public interface RepositoryHandler<R> {
	R handle(Repository repo) throws Exception;
}