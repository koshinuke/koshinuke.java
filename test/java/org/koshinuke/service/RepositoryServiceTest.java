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
import org.koshinuke._;
import org.koshinuke.conf.Configuration;
import org.koshinuke.git.GitHandler;
import org.koshinuke.git.GitUtil;
import org.koshinuke.jersey.TestConfigurationtProvider;
import org.koshinuke.jersey.TestPrincipalProvider;
import org.koshinuke.model.NodeModel;
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

	Configuration config;

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
		this.config = new TestConfigurationtProvider().getValue();
		this.deleteDirs();
	}

	public void deleteDirs() throws IOException {
		List<File> dirs = new ArrayList<>();
		dirs.add(new File("bin", "testInit"));
		dirs.add(this.config.getRepositoryRootDir().toFile().getParentFile());
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
		final File dest = new File("bin", "testInit");
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

		Path repo = this.config.getRepositoryRootDir().resolve(path);
		assertTrue(Files.exists(repo));

		GitUtil.handleClone(repo.toUri(), dest, new GitHandler<_>() {
			@Override
			public _ handle(Git g) throws Exception {
				File R = new File(dest, "README");
				assertTrue(R.exists());

				String destText = Resources.toString(R.toURI().toURL(),
						ReaderWriter.UTF8);
				assertEquals(readme, destText);
				return _._;
			}
		});
	}

	File cloneTestRepo() throws Exception {
		Path path = this.config.getRepositoryRootDir().resolve("proj/repo");
		final File testRepo = path.toFile();
		return GitUtil.handleClone(new File("test/repo").toPath().toUri(),
				testRepo, true, new GitHandler<File>() {
					@Override
					public File handle(Git git) throws Exception {
						assertTrue(testRepo.exists());
						return testRepo;
					}
				});
	}

	@Test
	public void testTree() throws Exception {
		this.cloneTestRepo();
		this.get(this.resource(), "dynamic/proj/repo/tree/master");
	}

	@Test
	public void testTreeWithParam() throws Exception {
		this.cloneTestRepo();
		List<NodeModel> list = this.get(
				this.resource().queryParam("offset", "1")
						.queryParam("limit", "4"),
				"dynamic/proj/repo/tree/branchbranch/");
		assertEquals(4, list.size());
	}

	@Test
	public void testTreeWithContext() throws Exception {
		this.cloneTestRepo();
		List<NodeModel> list = this.get(this.resource(),
				"dynamic/proj/repo/tree/branchbranch/hoge/piyo/");
		assertEquals(2, list.size());
	}

	protected List<NodeModel> get(WebResource webResource, String url)
			throws Exception {
		List<NodeModel> list = webResource.path(url)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(new GenericType<List<NodeModel>>() {
				});
		System.out.println("======================");
		for (NodeModel nm : list) {
			System.out.printf("%s %s %n", nm.getChildren(), nm.getPath());
		}
		assertNotNull(list);
		return list;
	}

}
