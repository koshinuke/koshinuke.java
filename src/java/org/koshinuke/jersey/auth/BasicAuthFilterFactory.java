package org.koshinuke.jersey.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import net.iharder.Base64;

import com.google.common.base.Charsets;
import com.google.common.net.HttpHeaders;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * @author taichi
 */
public class BasicAuthFilterFactory implements ResourceFilterFactory {

	@Context
	ThreadLocal<HttpServletRequest> requestInvoker;

	class BasicAuthenticationFilter extends AbstractRequestFilter {
		@Override
		public ContainerRequest filter(ContainerRequest request) {
			try {
				String authorization = request.getRequestHeaders().getFirst(
						HttpHeaders.AUTHORIZATION);
				if (authorization != null) {
					int space = authorization.indexOf(' ');
					if (0 < space) {
						String type = authorization.substring(0, space);
						if ("Basic".equalsIgnoreCase(type)) {
							String cred = authorization.substring(space + 1);

							// TODO performance test.
							cred = new String(Base64.decode(cred
									.getBytes(Charsets.ISO_8859_1)),
									Charsets.ISO_8859_1);
							int i = cred.indexOf(':');
							if (0 < i) {
								String u = cred.substring(0, i);
								String p = cred.substring(i + 1);
								HttpServletRequest req = BasicAuthFilterFactory.this.requestInvoker
										.get();
								req.login(u, p);
								return request;
							}
						}
					}
				}
			} catch (IOException | ServletException e) {
				throw new WebApplicationException(e, this.make401());
			}
			throw new WebApplicationException(this.make401());
		}

		protected Response make401() {
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.header(HttpHeaders.WWW_AUTHENTICATE,
							"Basic realm=\"koshinuke realm\"").build();
		}
	}

	@Override
	public List<ResourceFilter> create(AbstractMethod am) {
		if (am.isAnnotationPresent(BasicAuth.class)
				|| am.getResource().isAnnotationPresent(BasicAuth.class)) {
			return Collections
					.<ResourceFilter> singletonList(new BasicAuthenticationFilter());
		}
		return null;
	}

}
