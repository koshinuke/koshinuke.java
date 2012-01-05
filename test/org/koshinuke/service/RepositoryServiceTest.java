package org.koshinuke.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.koshinuke.model.Repository;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.spi.container.grizzly2.GrizzlyTestContainerFactory;

public class RepositoryServiceTest extends JerseyTest {

	RepositoryService target;

	public RepositoryServiceTest() {
		super(new GrizzlyTestContainerFactory());
	}

	@Override
	protected AppDescriptor configure() {
		ResourceConfig rc = new ClassNamesResourceConfig(
				RepositoryService.class);
		rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
		return new LowLevelAppDescriptor.Builder(rc).build();
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.target = new RepositoryService();
	}

	@Test
	public void testService() throws Exception {
		WebResource webResource = resource();
		String responseMsg = webResource.path("a/b/tree/c").get(String.class);
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
