package org.koshinuke.service;

import javax.ws.rs.core.Response;

/**
 * @author taichi
 */
public interface InfoRefsAction {

	Response execute(String project, String repository);
}
