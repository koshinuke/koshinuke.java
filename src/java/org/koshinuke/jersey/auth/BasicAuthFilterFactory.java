package org.koshinuke.jersey.auth;

import java.util.Collections;
import java.util.List;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * @author taichi
 */
public class BasicAuthFilterFactory implements ResourceFilterFactory {

	class BasicAuthenticationFilter implements ResourceFilter,
			ContainerRequestFilter {
		@Override
		public ContainerRequestFilter getRequestFilter() {
			return this;
		}

		@Override
		public ContainerResponseFilter getResponseFilter() {
			return null;
		}

		@Override
		public ContainerRequest filter(ContainerRequest request) {
			// TODO Auto-generated method stub
			return null;
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
