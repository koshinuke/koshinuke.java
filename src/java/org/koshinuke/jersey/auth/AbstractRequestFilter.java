package org.koshinuke.jersey.auth;

import java.util.logging.Logger;

import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

/**
 * @author taichi
 */
public abstract class AbstractRequestFilter implements ResourceFilter,
		ContainerRequestFilter {

	protected Logger LOG = Logger.getLogger(AbstractRequestFilter.class
			.getCanonicalName());

	@Override
	public ContainerRequestFilter getRequestFilter() {
		return this;
	}

	@Override
	public ContainerResponseFilter getResponseFilter() {
		return null;
	}
}