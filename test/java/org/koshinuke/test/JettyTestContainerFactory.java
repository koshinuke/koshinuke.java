package org.koshinuke.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.koshinuke.conf.Configuration;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

/**
 * @author taichi
 */
public class JettyTestContainerFactory implements TestContainerFactory {

	@Override
	@SuppressWarnings("unchecked")
	public Class<SimpleAppDescriptor> supports() {
		return SimpleAppDescriptor.class;
	}

	@Override
	public TestContainer create(URI baseUri, AppDescriptor ad)
			throws IllegalArgumentException {
		return new JettyTestContainer(baseUri,
				SimpleAppDescriptor.class.cast(ad));
	}

	static class JettyTestContainer implements TestContainer {

		Server server;
		URI baseUri;

		public JettyTestContainer(URI baseUri, SimpleAppDescriptor ad)
				throws IllegalArgumentException {
			this.baseUri = baseUri;

			try {
				ServletHolder holder = new ServletHolder(ServletContainer.class);
				holder.setInitParameter(
						ServletContainer.APPLICATION_CONFIG_CLASS, ad
								.getApplicationClass().getName());
				holder.setInitParameter(ContainerRequestFilter.class.getName(),
						GZIPContentEncodingFilter.class.getName());
				holder.setInitParameter(
						ContainerResponseFilter.class.getName(),
						GZIPContentEncodingFilter.class.getName());
				ServletContextHandler sch = new ServletContextHandler();
				sch.setAttribute(Configuration.NAME, new File(
						"src/test/koshinuke-test.properties").toURI().toURL());
				sch.setResourceBase("src/webapp");
				sch.addServlet(holder, "/*");
				this.server = new Server(this.baseUri.getPort());
				this.server.setHandler(sch);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(e);
			}
		}

		@Override
		public Client getClient() {
			return null;
		}

		@Override
		public URI getBaseUri() {
			return this.baseUri;
		}

		@Override
		public void start() {
			try {
				this.server.start();
			} catch (Exception e) {
				throw new TestContainerException(e);
			}
		}

		@Override
		public void stop() {
			try {
				if (this.server.isStarted()) {
					this.server.stop();
				}
			} catch (Exception e) {
				throw new TestContainerException(e);
			}
		}

	}
}
