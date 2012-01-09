package org.koshinuke;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.koshinuke.service.LoginService;
import org.koshinuke.service.RepositoryService;

/**
 * @author taichi
 */
public class App extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(LoginService.class);
		classes.add(RepositoryService.class);
		return classes;
	}
}
