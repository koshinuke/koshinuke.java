package org.koshinuke;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.koshinuke.jersey.StaticViewProcessor;
import org.koshinuke.service.ServiceModule;
import org.koshinuke.soy.SoyTemplatesModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * @author taichi
 */
public class App extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new JerseyServletModule() {
			@Override
			protected void configureServlets() {
				install(new ServiceModule());
				install(new SoyTemplatesModule());
				bind(JacksonJsonProvider.class).in(Singleton.class);
				bind(JacksonJsonProvider.class).in(Singleton.class);
				bind(StaticViewProcessor.class).in(Singleton.class);
				serve("/*")
						.with(GuiceContainer.class,
								ImmutableMap
										.of(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX,
												"/[sS][tT][aA][tT][iI][cC]/.*"));
			}
		});
	}
}
