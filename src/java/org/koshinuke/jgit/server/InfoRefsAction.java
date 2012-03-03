package org.koshinuke.jgit.server;

import javax.ws.rs.core.Response;

import org.koshinuke.model.KoshinukePrincipal;

/**
 * @author taichi
 */
public interface InfoRefsAction {

	Response execute(KoshinukePrincipal principal, String project,
			String repository);
}
