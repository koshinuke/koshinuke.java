package org.koshinuke.jersey;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.eclipse.jgit.util.StringUtils;
import org.koshinuke.jersey.auth.AbstractRequestFilter;
import org.koshinuke.util.ServletUtil;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * @author taichi
 */
public class CsrfFilterFactory implements ResourceFilterFactory {

	@Context
	ThreadLocal<HttpServletRequest> requestInvoker;

	static final Pattern METHODS = Pattern.compile("GET|HEAD",
			Pattern.CASE_INSENSITIVE);
	static final String KOSHINUKE = "X-KoshiNuke";

	class CsrfFilter extends AbstractRequestFilter {
		@Override
		public ContainerRequest filter(ContainerRequest request) {
			if (this.verify(CsrfFilterFactory.this.requestInvoker.get())) {
				return request;
			}
			throw new WebApplicationException(Response
					.status(HttpServletResponse.SC_MOVED_TEMPORARILY)
					.location(request.getBaseUri()).build());
		}

		protected boolean verify(HttpServletRequest request) {
			String method = request.getMethod();
			return StringUtils.isEmptyOrNull(method) == false
					&& (METHODS.matcher(method).matches() || ServletUtil
							.verifyCsrf(request.getSession(false),
									request.getHeader(KOSHINUKE)));
		}
	}

	@Override
	public List<ResourceFilter> create(AbstractMethod am) {
		if (am.isAnnotationPresent(Csrf.class)
				|| am.getResource().isAnnotationPresent(Csrf.class)) {
			return Collections.<ResourceFilter> singletonList(new CsrfFilter());
		}
		return null;
	}

}
