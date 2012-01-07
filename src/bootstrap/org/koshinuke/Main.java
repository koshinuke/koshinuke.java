package org.koshinuke;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.eclipse.jetty.plus.jaas.JAASLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.FileResource;
import org.koshinuke.service.RepositoryService;

import com.sun.jersey.spi.container.servlet.ServletContainer;

public class Main extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(RepositoryService.class);
		return classes;
	}

	protected static Server start() throws Exception {
		Server server = new Server(9998);
		ServletHolder holder = new ServletHolder(ServletContainer.class);
		holder.setInitParameter(ServletContainer.APPLICATION_CONFIG_CLASS,
				Main.class.getName());
		holder.setInitParameter(
				ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX,
				"/(images|js|css)/.*|.*\\.html");
		ServletContextHandler sch = new ServletContextHandler(
				ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);
		sch.addServlet(holder, "/*");
		setUpResource(sch);
		server.setHandler(sch);
		// security settings.
		SecurityHandler secure = setUpSecurityHandler(sch);
		server.start();
		secure.start();
		return server;
	}

	private static void setUpResource(ServletContextHandler handler)
			throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL url = cl.getResource("META-INF/resources");
		handler.setBaseResource(new FileResource(url));
	}

	private static SecurityHandler setUpSecurityHandler(
			ServletContextHandler sch) throws Exception {
		System.setProperty("java.security.auth.login.config", "etc/jaas.conf");
		SecurityHandler secure = sch.getSecurityHandler();
		secure.setAuthMethod("FORM");
		secure.setRealmName("Login");
		JAASLoginService ls = new JAASLoginService(secure.getRealmName());
		ls.start();
		secure.setLoginService(ls);
		return secure;
	}

	public static void main(String[] args) throws Exception {
		Server server = start();

		System.in.read();
		server.stop();
	}

}
