package org.koshinuke.jersey.auth;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.koshinuke.model.DefaultKoshinukePrincipal;
import org.koshinuke.model.KoshinukePrincipal;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * @author taichi
 */
public class AuthenticationFilterFactory implements ResourceFilterFactory {

	public static final String AUTH = AuthenticationFilter.class.getName()
			+ ".auth";

	@Context
	ThreadLocal<HttpServletRequest> requestInvoker;

	class AuthenticationFilter extends AbstractRequestFilter {
		@Override
		public ContainerRequest filter(ContainerRequest request) {
			HttpServletRequest req = AuthenticationFilterFactory.this.requestInvoker
					.get();
			if (isLoggedIn(req)) {
				return request;
			}
			throw new WebApplicationException(Response
					.status(HttpServletResponse.SC_MOVED_TEMPORARILY)
					.location(
							request.getBaseUriBuilder().path("/login").build())
					.build());
		}
	}

	public static boolean isLoggedIn(HttpServletRequest request) {
		return getUserPrincipal(request) != null;
	}

	public static KoshinukePrincipal getUserPrincipal(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			return (KoshinukePrincipal) session.getAttribute(AUTH);
		}
		return (KoshinukePrincipal) request.getAttribute(AUTH);
	}

	/**
	 * set principal to http session.
	 * 
	 * @param request
	 */
	public static void setUserPrincipal(HttpSession session,
			HttpServletRequest request) {
		session.setAttribute(AUTH, of(request));
	}

	protected static KoshinukePrincipal of(HttpServletRequest request) {
		Principal principal = request.getUserPrincipal();
		return new DefaultKoshinukePrincipal(principal);
	}

	/**
	 * set principal to http request.
	 * 
	 * @param request
	 */
	public static void setUserPrincipal(HttpServletRequest request) {
		request.setAttribute(AUTH, of(request));
	}

	@Override
	public List<ResourceFilter> create(AbstractMethod am) {
		if (am.isAnnotationPresent(Auth.class)
				|| am.getResource().isAnnotationPresent(Auth.class)) {
			return Collections
					.<ResourceFilter> singletonList(new AuthenticationFilter());
		}
		return null;
	}

}
