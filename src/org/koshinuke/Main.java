package org.koshinuke;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.koshinuke.service.RepositoryService;

import com.sun.jersey.api.core.ApplicationAdapter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class Main extends Application {
	
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(RepositoryService.class);
		return classes;
	}
	
//	private static URI getBaseURI() {
//		return UriBuilder.fromUri("http://localhost").port(9998).build();
//	}
	
	protected static Server start()  throws Exception {
		ResourceConfig rc = new ApplicationAdapter(new Main());
		rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);

		Server server = new Server(9998);
		server.addConnector(new SelectChannelConnector());
		ServletHolder holder = new ServletHolder(ServletContainer.class);
		holder.setInitParameter(ServletContainer.APPLICATION_CONFIG_CLASS, Main.class.getName());
		holder.setInitParameter(JSONConfiguration.FEATURE_POJO_MAPPING, String.valueOf(true));
		ServletContextHandler sch = new ServletContextHandler(ServletContextHandler.SESSIONS);
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
