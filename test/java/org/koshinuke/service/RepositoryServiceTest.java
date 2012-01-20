package org.koshinuke.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.koshinuke.jersey.TestConfigurationtProvider;
import org.koshinuke.jersey.TestPrincipalProvider;
import org.koshinuke.model.BlobModel;
import org.koshinuke.model.NodeModel;
import org.koshinuke.model.RepositoryModel;
import org.koshinuke.test.KoshinukeTest;
import org.koshinuke.util.FileUtil;
import org.koshinuke.util.GitUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

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

		GitUtil.handleClone(repo.toUri(), dest, new Function<Git, _>() {
			@Override
			public _ apply(Git g) {
				try {
					File R = new File(dest, "README");
					assertTrue(R.exists());

					String destText = Resources.toString(R.toURI().toURL(),
							Charsets.UTF_8);
					assertEquals(readme, destText);
				} catch (IOException e) {
					throw new AssertionError(e);
				}
				return _._;
			}
		});
	}

	File cloneTestRepo() throws Exception {
		Path path = this.config.getRepositoryRootDir().resolve("proj/repo");
		final File testRepo = path.toFile();
		return GitUtil.handleClone(new File("test/repo").toPath().toUri(),
				testRepo, true, new Function<Git, File>() {
					@Override
					public File apply(Git git) {
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
		assertEquals(0, list.get(1).getTimestamp());
		assertEquals(1, list.get(1).getChildren());
		assertEquals(0, list.get(2).getTimestamp());
		assertEquals(2, list.get(2).getChildren());
	}

	@Test
	public void testTreeWithContext() throws Exception {
		this.cloneTestRepo();
		List<NodeModel> list = this.get(this.resource(),
				"dynamic/proj/repo/tree/branchbranch/moge/");
		assertEquals(3, list.size());

		list = this.get(this.resource(),
				"dynamic/proj/repo/tree/branchbranch/hoge/piyo");
		assertEquals(2, list.size());
		assertFalse(0 == list.get(0).getTimestamp());
		assertEquals(0, list.get(0).getChildren());

	}

	@Test
	public void testTreeWithTag() throws Exception {
		this.cloneTestRepo();
		List<NodeModel> list = this.get(this.resource(),
				"dynamic/proj/repo/tree/beta/0.0.1");
		assertEquals(4, list.size());
	}

	protected List<NodeModel> get(WebResource webResource, String url)
			throws Exception {
		List<NodeModel> list = webResource.path(url)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(new GenericType<List<NodeModel>>() {
				});
		System.out.println("======================");
		assertNotNull(list);
		Collections.sort(list, new Comparator<NodeModel>() {
			@Override
			public int compare(NodeModel l, NodeModel r) {
				return l.getPath().compareTo(r.getPath());
			}
		});
		for (NodeModel nm : list) {
			System.out.printf("%s %10s %s %s %n", nm.getChildren(),
					nm.getTimestamp(), nm.getPath(), nm.getMessage());
		}
		return list;
	}

	@Test
	public void testBlob() throws Exception {
		this.cloneTestRepo();
		BlobModel bm = this.resource()
				.path("/dynamic/proj/repo/blob/master/README")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(BlobModel.class);
		assertNotNull(bm);
		assertEquals("readme readme", bm.getContents());
		assertEquals("initial commit", bm.getMessage());

		bm = this.resource()
				.path("/dynamic/proj/repo/blob/test/hoge/hoge/moge/piro.txt")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(BlobModel.class);
		assertNotNull(bm);
		assertEquals("gyappa gyappa", bm.getContents());
		assertEquals("gyawawa", bm.getMessage());
		assertEquals("monster1", bm.getAuthor());

	}

	@Test
	public void testModifyBlob() throws Exception {
		this.cloneTestRepo();
		String path = "/dynamic/proj/repo/blob/master/README";
		BlobModel bm = this.resource().path(path)
				.accept(MediaType.APPLICATION_JSON_TYPE).get(BlobModel.class);
		assertNotNull(bm);

		BlobModel newone = new BlobModel(bm);
		newone.setMessage("modifiy!!");
		newone.setContents(bm.getContents() + "\nhogehoge");

		BlobModel modified = this.resource().path(path)
				.accept(MediaType.APPLICATION_JSON)
				.entity(newone, MediaType.APPLICATION_JSON)
				.post(BlobModel.class);
		assertNotNull(modified);

		assertNotSame(bm.getTimestamp(), modified.getTimestamp());
		assertEquals(newone.getMessage(), modified.getMessage());
		assertEquals(newone.getContents(), modified.getContents());
	}
}
