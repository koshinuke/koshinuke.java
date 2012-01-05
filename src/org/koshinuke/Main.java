package org.koshinuke;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.koshinuke.service.RepositoryService;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.ApplicationAdapter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class Main extends Application {
	
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(RepositoryService.class);
		return classes;
	}
	
	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost").port(9998).build();
	}
	
	public static void main(String[] args) throws Exception {
		ResourceConfig rc = new ApplicationAdapter(new Main());
		rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
		HttpServer httpServer = GrizzlyServerFactory.createHttpServer(getBaseURI(), rc);
		
		System.in.read();
		httpServer.stop();
	}

}
