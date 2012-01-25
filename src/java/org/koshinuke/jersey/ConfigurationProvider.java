package org.koshinuke.jersey;

import java.net.URL;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.koshinuke.conf.Configuration;
import org.koshinuke.conf.PropertiesConfiguration;

import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import com.sun.jersey.spi.resource.Singleton;

/**
 * @author taichi
 */
@Singleton
@Provider
public class ConfigurationProvider extends
		SingletonTypeInjectableProvider<Context, Configuration> {

	public ConfigurationProvider(@Context ServletContext context)
			throws Exception {
		super(Configuration.class, configure(context));
	}

	static Configuration configure(ServletContext context) throws Exception {
		URL url = (URL) context.getAttribute(Configuration.NAME);
		Configuration conf = new PropertiesConfiguration();
		conf.configure(url);
		return conf;
	}
}
