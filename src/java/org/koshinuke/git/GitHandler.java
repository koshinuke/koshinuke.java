package org.koshinuke.git;

import org.eclipse.jgit.api.Git;

/**
 * @author taichi
 * @param <R>
 */
public interface GitHandler<R> {
	R handle(Git git) throws Exception;
}