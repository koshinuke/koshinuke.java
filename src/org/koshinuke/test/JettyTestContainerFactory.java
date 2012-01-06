package org.koshinuke.test;

import java.net.InetSocketAddress;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

public class JettyTestContainerFactory implements TestContainerFactory {

	@Override
	@SuppressWarnings("unchecked")
	public Class<WebAppDescriptor> supports() {
		return WebAppDescriptor.class;
	}

	@Override
	public TestContainer create(URI baseUri, AppDescriptor ad)
			throws IllegalArgumentException {
		return new JettyTestContainer(baseUri, WebAppDescriptor.class.cast(ad));
	}

	static class JettyTestContainer implements TestContainer {

		Server server;
		URI baseUri;

		public JettyTestContainer(URI baseUri, WebAppDescriptor ad) {
			this.baseUri = UriBuilder.fromUri(baseUri)
					.path(ad.getContextPath()).path(ad.getServletPath())
					.build();

			this.server = new Server(InetSocketAddress.createUnresolved(
					this.baseUri.getHost(), this.baseUri.getPort()));
			ServletContextHandler sch = new ServletContextHandler(this.server,
					ad.getContextPath());

			if (ad.getServletClass() != null) {
				sch.addServlet(ad.getServletClass(), ad.getServletPath());
			}
		}

		@Override
		public Client getClient() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public URI getBaseUri() {
			return this.baseUri;
		}

		@Override
		public void start() {
			try {
				server.start();
			} catch (Exception e) {
				throw new TestContainerException(e);
			}
		}

		@Override
		public void stop() {
			try {
				if (server.isStarted()) {
					server.stop();
				}
			} catch (Exception e) {
				throw new TestContainerException(e);
			}
		}

	}
}
