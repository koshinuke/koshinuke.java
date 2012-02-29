package org.koshinuke.service;

import javax.ws.rs.core.Response;

import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;

/**
 * @author taichi
 */
public interface InfoRefsAction {

	Response execute(String project, String repository)
			throws ServiceNotEnabledException;
}
