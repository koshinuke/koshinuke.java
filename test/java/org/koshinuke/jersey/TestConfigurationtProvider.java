package org.koshinuke.jersey;

import java.net.URL;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.koshinuke.conf.Configuration;
import org.koshinuke.conf.PropertiesConfiguration;

import com.google.common.io.Resources;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import com.sun.jersey.spi.resource.Singleton;

/**
 * @author taichi
 */
@Singleton
@Provider
public class TestConfigurationtProvider extends
		SingletonTypeInjectableProvider<Context, Configuration> {

	public TestConfigurationtProvider() throws Exception {
		super(Configuration.class, configure());
	}

	static Configuration configure() throws Exception {
		URL url = Resources.getResource("koshinuke-test.properties");
		Configuration conf = new PropertiesConfiguration();
		conf.configure(url);
		return conf;
	}
}
