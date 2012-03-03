package org.koshinuke.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;
import org.koshinuke.App;
import org.koshinuke._;
import org.koshinuke.jersey.TestConfigurationtProvider;
import org.koshinuke.jersey.TestPrincipalProvider;
import org.koshinuke.jgit.server.EachRefPackWriter;
import org.koshinuke.jgit.server.GitHttpdService;
import org.koshinuke.jgit.server.ReceivePackWriter;
import org.koshinuke.jgit.server.UploadPackWriter;
import org.koshinuke.model.BlameModel;
import org.koshinuke.model.BlobModel;
import org.koshinuke.model.BranchHistoryModel;
import org.koshinuke.model.CommitModel;
import org.koshinuke.model.DiffEntryModel;
import org.koshinuke.model.DiffModel;
import org.koshinuke.model.NodeModel;
import org.koshinuke.model.RepositoryModel;
import org.koshinuke.test.KoshinukeTest;
import org.koshinuke.util.GitUtil;
import org.koshinuke.util.ServletUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

/**
 * @author taichi
 */
public class RepositoryServiceTest extends KoshinukeTest {

	public static class AP extends Application {
		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> s = new HashSet<>();
			s.add(RepositoryService.class);
			s.add(GitHttpdService.class);
			s.add(TestConfigurationtProvider.class);
			s.add(TestPrincipalProvider.class);
			return s;
		}

		@Override
		public Set<Object> getSingletons() {
			HashSet<Object> singletons = new HashSet<Object>();
			singletons.add(App.makeJsonProvider());
			singletons.add(new EachRefPackWriter());
			singletons.add(new UploadPackWriter());
			singletons.add(new ReceivePackWriter());
			return singletons;
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
		deleteDirs();
	}

	@Test
	public void testListNoRepositories() throws Exception {
		List<RepositoryModel> list = this.resource().path("/api/1.0")
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
		form.add("!", "init");
		form.add("rn", path);
		form.add("rr", readme);

		List<RepositoryModel> list = this.resource().path("/api/1.0")
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

		Path repo = config.getRepositoryRootDir().resolve(path);
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

	@Test
	public void testClone() throws Exception {
		this.cloneTestRepo();

		Form form = new Form();
		form.add("!", "clone");
		form.add("uri", this.getBaseURI().resolve("/proj/repo.git").toString());
		form.add("un", "username");
		form.add("up", "password");

		List<RepositoryModel> list = this.resource().path("/api/1.0")
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
				.post(new GenericType<List<RepositoryModel>>() {
				}, form);
		assertNotNull(list);
		assertEquals(2, list.size());
	}

	@Test
	public void testTree() throws Exception {
		this.cloneTestRepo();
		this.get(this.resource(), "api/1.0/proj/repo/tree/master");
	}

	@Test
	public void testTreeWithParam() throws Exception {
		this.cloneTestRepo();
		List<NodeModel> list = this.get(
				this.resource().queryParam("offset", "1")
						.queryParam("limit", "4"),
				"api/1.0/proj/repo/tree/branchbranch/");
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
				"api/1.0/proj/repo/tree/branchbranch/moge/");
		assertEquals(3, list.size());

		list = this.get(this.resource(),
				"api/1.0/proj/repo/tree/branchbranch/hoge/piyo");
		assertEquals(2, list.size());
		assertFalse(0 == list.get(0).getTimestamp());
		assertEquals(0, list.get(0).getChildren());

	}

	@Test
	public void testTreeWithTag() throws Exception {
		this.cloneTestRepo();
		List<NodeModel> list = this.get(this.resource(),
				"api/1.0/proj/repo/tree/beta/0.0.1");
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
				.path("/api/1.0/proj/repo/blob/master/README")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(BlobModel.class);
		assertNotNull(bm);
		assertEquals("readme readme", bm.getContent());
		assertEquals("initial commit", bm.getMessage());

		bm = this.resource()
				.path("/api/1.0/proj/repo/blob/test/hoge/hoge/moge/piro.txt")
				.accept(MediaType.APPLICATION_JSON_TYPE).get(BlobModel.class);
		assertNotNull(bm);
		assertEquals("gyappa gyappa", bm.getContent());
		assertEquals("gyawawa", bm.getMessage());
		assertEquals("monster1", bm.getAuthor());

	}

	@Test
	public void testModifyBlob() throws Exception {
		this.cloneTestRepo();
		{
			String path = "/api/1.0/proj/repo/blob/master/README";
			BlobModel bm = this.getBlob(path);

			BlobModel newone = new BlobModel(bm);
			newone.setMessage("modifiy!!");
			newone.setContent(bm.getContent() + "\nhogehoge");

			this.testModifyBlob(path, newone);
		}
		String path = "/api/1.0/proj/repo/blob/test/hoge/hoge/moge/piro.txt";
		BlobModel bm = this.getBlob(path);
		BlobModel newone = new BlobModel(bm);
		newone.setMessage("mod mod");
		newone.setContent(bm.getContent() + "\nhogehoge");

		this.testModifyBlob(path, newone);
	}

	@Test
	public void testModiryErrorBecauseTagCannotModify() throws Exception {
		this.cloneTestRepo();
		String path = "/api/1.0/proj/repo/blob/beta/0.0.2/myomyo/muga/piyopiyo.txt";
		BlobModel bm = this.getBlob(path);
		BlobModel newone = new BlobModel(bm);
		newone.setMessage("mod mod");
		newone.setContent(bm.getContent() + "\nhogehoge");
		try {
			this.testModifyBlob(path, newone);
			fail();
		} catch (UniformInterfaceException e) {
			assertEquals(ServletUtil.SC_UNPROCESSABLE_ENTITY, e.getResponse()
					.getStatus());
		}
	}

	protected BlobModel getBlob(String path) {
		BlobModel bm = this.resource().path(path)
				.accept(MediaType.APPLICATION_JSON_TYPE).get(BlobModel.class);
		assertNotNull(bm);
		return bm;
	}

	protected void testModifyBlob(String path, BlobModel newone) {
		BlobModel modified = this.resource().path(path)
				.accept(MediaType.APPLICATION_JSON)
				.entity(newone, MediaType.APPLICATION_JSON)
				.post(BlobModel.class);
		assertNotNull(modified);

		assertNotSame(newone.getTimestamp(), modified.getTimestamp());
		assertEquals(newone.getMessage(), modified.getMessage());
		assertEquals(newone.getContent(), modified.getContent());
	}

	@Test
	public void testHistories() throws Exception {
		this.setUpTestHistories(this.cloneTestRepo());
		String path = "/api/1.0/proj/repo/history";
		List<BranchHistoryModel> list = this.resource().path(path)
				.get(new GenericType<List<BranchHistoryModel>>() {
				});
		assertNotNull(list);
		for (BranchHistoryModel model : list) {
			assertTrue(0 != model.getTimestamp());
			assertEquals(model.getName(), 30, model.getActivities().size());
			assertEquals(model.getPath(), model.getName());
			System.out.printf("%12s ", model.getName());
			for (long[] a : model.getActivities()) {
				System.out.printf("[%s %s] ", new SimpleDateFormat("MM-dd")
						.format(new Date(a[0] * 1000)), a[1]);
			}
			System.out.println();
			if (model.getName().equals("test/hoge")) {
				assertEquals(1, model.getActivities().get(29)[1]);
			}
		}
	}

	protected void setUpTestHistories(final File f) throws Exception {
		final File working = new File("bin", "testHist");
		GitUtil.handleClone(f.toURI(), working, new Function<Git, _>() {
			@Override
			public _ apply(Git g) {
				try {
					String sp = "test/hoge";
					g.checkout()
							.setCreateBranch(true)
							.setName(sp)
							.setStartPoint(Constants.R_REMOTES + "origin/" + sp)
							.call();
					String content = "ぎょぱぎょぱ";
					String path = "ppp/zzz/gg.txt";
					File newone = new File(working, path);
					newone.getParentFile().mkdirs();
					com.google.common.io.Files.write(content, newone,
							java.nio.charset.Charset.forName("UTF-8"));
					g.add().addFilepattern(path).call();
					g.commit().setMessage("ぎょっわ")
							.setAuthor("testtest", "testHist@koshinuke.org")
							.call();
					g.push().call();
					return _._;
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}

	@Test
	public void testGetCommits() throws Exception {
		this.setUpTestHistories(this.cloneTestRepo());
		String path = "/api/1.0/proj/repo/commits/test/moge";
		List<CommitModel> list = this.resource().path(path)
				.get(new GenericType<List<CommitModel>>() {
				});
		assertNotNull(list);
		assertEquals(3, list.size());
		assertEquals("ぐわわ…", list.get(0).getMessage());
		assertEquals("gyawawa", list.get(1).getMessage());
		assertEquals("initial commit", list.get(2).getMessage());

	}

	protected String setUpTestDiff(File f) throws Exception {
		final File working = new File("bin", "testDiff");
		return GitUtil.handleClone(f.toURI(), working,
				new Function<Git, String>() {
					@Override
					public String apply(Git g) {
						try {
							String sp = "test/hoge";
							g.checkout()
									.setCreateBranch(true)
									.setName(sp)
									.setStartPoint(
											Constants.R_REMOTES + "origin/"
													+ sp).call();
							AddCommand add = g.add();
							{
								String content = "ぎょぱぎょぱ";
								String path = "ppp/zzz/gg.txt";
								File newone = new File(working, path);
								newone.getParentFile().mkdirs();
								com.google.common.io.Files.write(content,
										newone, java.nio.charset.Charset
												.forName("UTF-8"));
								add.addFilepattern(path);
							}
							{
								String content = "gyappa \nguwawa\ngyowagyowa";
								String path = "hoge/moge/piro.txt";
								File newone = new File(working, path);
								com.google.common.io.Files.write(content,
										newone, java.nio.charset.Charset
												.forName("UTF-8"));
								add.addFilepattern(path);
							}
							add.call();

							RevCommit commit = g
									.commit()
									.setMessage("ぎょっわぎょわ")
									.setAuthor("testdiff",
											"testdiff@koshinuke.org").call();
							g.push().call();
							return commit.getId().name();
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					}
				});
	}

	@Test
	public void testDiff() throws Exception {
		String commitid = this.setUpTestDiff(this.cloneTestRepo());
		String path = "/api/1.0/proj/repo/commit/" + commitid;
		DiffModel model = this.resource().path(path).get(DiffModel.class);
		assertNotNull(model);
		assertEquals(commitid, model.getCommit().name());
		assertEquals(1, model.getParents().length);
		List<DiffEntryModel> list = model.getDiff();
		assertEquals(2, list.size());
		assertTrue(list.get(0).getPatch().startsWith("@@ -1 +1,3 @@"));
		assertTrue(list.get(1).getPatch().startsWith("@@ -0,0 +1 @@"));

		for (DiffEntryModel dem : list) {
			System.out.printf("[%s] %s -> %s %n", dem.getOperation(),
					dem.getOldPath(), dem.getNewPath());
			System.out.println("=============================================");
			System.out.println(dem.getOldContent());
			System.out.println("=============================================");
			System.out.println(dem.getPatch());
		}
	}

	@Test
	public void testBlame() throws Exception {
		this.setUpTestDiff(this.cloneTestRepo());
		List<BlameModel> list = this.resource()
				.path("/api/1.0/proj/repo/blame/test/hoge/hoge/moge/piro.txt")
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(new GenericType<List<BlameModel>>() {
				});

		assertNotNull(list);
		assertEquals(3, list.size());
		assertEquals("gyappa ", list.get(0).getContent());
		assertEquals("ぎょっわぎょわ", list.get(0).getMessage());
		for (BlameModel bm : list) {
			System.out.printf("%s %s [%-20s] %s%n", bm.getAuthor(),
					bm.getTimestamp(), bm.getMessage(), bm.getContent());
		}
	}
}
