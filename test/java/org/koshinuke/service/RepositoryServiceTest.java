package org.koshinuke.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.eclipse.jgit.api.Git;
import org.junit.Before;
import org.junit.Test;
import org.koshinuke.jersey.TestConfigurationtProvider;
import org.koshinuke.jersey.TestPrincipalProvider;
import org.koshinuke.model.Repository;
import org.koshinuke.model.RepositoryModel;
import org.koshinuke.test.KoshinukeTest;
import org.koshinuke.util.FileUtil;

import com.google.common.io.Resources;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.ReaderWriter;

/**
 * @author taichi
 */
public class RepositoryServiceTest extends KoshinukeTest {

	RepositoryService target;

	public static class AP extends Application {
		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> s = new HashSet<>();
			s.add(RepositoryService.class);
			s.add(TestConfigurationtProvider.class);
			s.add(TestPrincipalProvider.class);
			return s;
		}
	}

	@Override
	protected Class<? extends Application> getApplicationClass() {
		return AP.class;
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.target = new RepositoryService();
		this.target.config = new TestConfigurationtProvider().getValue();
	}

	@Before
	public void deleteDirs() throws IOException {
		List<File> dirs = new ArrayList<>();
		dirs.add(new File("bin", "testInit"));
		dirs.add(this.target.config.getRepositoryRootDir().toFile()
				.getParentFile());
		for (File p : dirs) {
			FileUtil.delete(p.getAbsolutePath());
		}
	}

	@Test
	public void testListNoRepositories() throws Exception {
		List<RepositoryModel> list = this.resource().path("/dynamic")
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(new GenericType<List<RepositoryModel>>() {
				});
		assertEquals(0, list.size());
	}

	@Test
	public void testInit() throws Exception {
		File dest = new File("bin", "testInit");
		final String readme = "readme text";
		final String path = "test/init";
		Form form = new Form();
		form.add("rn", path);
		form.add("rr", readme);

		List<RepositoryModel> list = this.resource().path("/dynamic")
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
				.post(new GenericType<List<RepositoryModel>>() {
				}, form);

		assertNotNull(list);
		assertEquals(1, list.size());
		RepositoryModel rm = list.get(0);
		assertEquals(rm.getPath(), path);
		assertEquals(rm.getName(), "init");
		assertEquals(1, rm.getBranches().size());
		assertEquals("master", rm.getBranches().get(0).getName());

		Path repo = this.target.config.getRepositoryRootDir().resolve(path);
		assertTrue(Files.exists(repo));

		Git g = null;
		try {
			g = Git.cloneRepository().setURI(repo.toUri().toString())
					.setDirectory(dest).call();
			File R = new File(dest, "README");
			assertTrue(R.exists());

			String destText = Resources.toString(R.toURI().toURL(),
					ReaderWriter.UTF8);
			assertEquals(readme, destText);
		} finally {
			if (g != null) {
				g.getRepository().close();
			}
		}
	}

	@Test
	public void testService() throws Exception {
		WebResource webResource = this.resource();
		String responseMsg = webResource.path("dynamic/a/b/tree/c")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
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
