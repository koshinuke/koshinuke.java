package org.koshinuke.git;

import org.eclipse.jgit.revwalk.RevWalk;

/**
 * @author taichi
 */
public interface RevWalkHandler<R> {

	R handle(RevWalk walk) throws Exception;
}
