package org.koshinuke;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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
		ServletContextHandler sch = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		sch.addServlet(holder, "/*");
		server.setHandler(sch);
		server.start();
		return server;
	}

	public static void main(String[] args) throws Exception {
		Server server = start();

		System.in.read();
		server.stop();
	}

}
