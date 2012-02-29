package org.koshinuke;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.koshinuke.jackson.KoshinukeModule;
import org.koshinuke.jackson.LowerCaseStrategy;
import org.koshinuke.jersey.ConfigurationProvider;
import org.koshinuke.jersey.KoshinukePrincipalProvider;
import org.koshinuke.jersey.auth.BasicAuthFilterFactory;
import org.koshinuke.service.GitHttpdService;
import org.koshinuke.service.RepositoryService;
import org.koshinuke.service.RootService;
import org.koshinuke.service.UserService;

import com.sun.jersey.api.core.DefaultResourceConfig;

/**
 * @author taichi
 */
public class App extends DefaultResourceConfig {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<>();
		classes.add(RootService.class);
		classes.add(GitHttpdService.class);
		classes.add(UserService.class);
		classes.add(RepositoryService.class);
		classes.add(ConfigurationProvider.class);
		classes.add(KoshinukePrincipalProvider.class);
		return classes;
	}

	@Override
	public Set<Object> getSingletons() {
		HashSet<Object> singletons = new HashSet<Object>();
		singletons.add(makeJsonProvider());
		return singletons;
	}

	public static JacksonJsonProvider makeJsonProvider() {
		ObjectMapper om = new ObjectMapper();
		om.setPropertyNamingStrategy(new LowerCaseStrategy());
		om.configure(Feature.ESCAPE_NON_ASCII, true);
		om.registerModule(new KoshinukeModule());
		return new JacksonJsonProvider(om);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getResourceFilterFactories() {
		return Collections.singletonList(BasicAuthFilterFactory.class);
	}
}
