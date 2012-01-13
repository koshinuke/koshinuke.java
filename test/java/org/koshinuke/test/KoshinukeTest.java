package org.koshinuke.test;

import javax.ws.rs.core.Application;

import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

/**
 * @author taichi
 */
public abstract class KoshinukeTest extends JerseyTest {

	@Override
	protected TestContainerFactory getTestContainerFactory()
			throws TestContainerException {
		return new JettyTestContainerFactory();
	}

	@Override
	protected AppDescriptor configure() {
		return new SimpleAppDescriptor.Builder(this.getApplicationClass())
				.build();
	}

	protected abstract Class<? extends Application> getApplicationClass();
}
