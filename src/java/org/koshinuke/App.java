package org.koshinuke;

import org.koshinuke.filter.AuthenticationFilter;
import org.koshinuke.filter.EncodingFilter;
import org.koshinuke.jersey.SoyViewProcessor;
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
				bind(SoyViewProcessor.class).in(Singleton.class);
				serve("/*")
						.with(GuiceContainer.class,
								ImmutableMap
										.of(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX,
												"/[sS][tT][aA][tT][iI][cC]/.*"));
				FilterKeyBindingBuilder filters = filter("/dynamic*", "/login");
				filters.through(EncodingFilter.class);
				filters.through(AuthenticationFilter.class);
			}
		});
	}
}
