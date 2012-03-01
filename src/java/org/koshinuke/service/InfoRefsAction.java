package org.koshinuke.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

/**
 * @author taichi
 */
public interface InfoRefsAction {

	Response execute(HttpServletRequest request, String project,
			String repository);
}
