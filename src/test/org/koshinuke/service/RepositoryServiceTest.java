package org.koshinuke.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.junit.Before;
import org.junit.Test;
import org.koshinuke.model.Repository;
import org.koshinuke.test.JettyTestContainerFactory;
import org.koshinuke.test.SimpleAppDescriptor;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

public class RepositoryServiceTest extends JerseyTest {

	RepositoryService target;

	public static class AP extends Application {
		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> s = new HashSet<Class<?>>();
			s.add(RepositoryService.class);
			return s;
		}
	}

	@Override
	protected TestContainerFactory getTestContainerFactory()
			throws TestContainerException {
		return new JettyTestContainerFactory();
	}

	@Override
	protected AppDescriptor configure() {
		return new SimpleAppDescriptor.Builder(AP.class).build();
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.target = new RepositoryService();
	}

	@Test
	public void testService() throws Exception {
		WebResource webResource = resource();
		String responseMsg = webResource.path("dynamic/a/b/tree/c")
				.header("X-Requested-With", "XMLHttpRequest").get(String.class);
		assertNotNull(responseMsg);
	}

	@Test
	public void testName() {
		Repository r = this.target.name("a", "b");
		assertEquals(r.path, "a");
		assertEquals(r.name, "b");
	}

	@Test
	public void testTree() {
		assertNotNull(this.target.tree("a", "b", "c"));
	}

}
