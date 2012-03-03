package org.koshinuke.jgit.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.junit.Before;
import org.junit.Test;
import org.koshinuke.App;
import org.koshinuke.jersey.TestConfigurationtProvider;
import org.koshinuke.jersey.TestPrincipalProvider;
import org.koshinuke.test.KoshinukeTest;
import org.koshinuke.util.GitUtil;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * @author taichi
 */
public class GitHttpdServiceTest extends KoshinukeTest {

	public static class AP extends Application {
		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> s = new HashSet<>();
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
	public void testClone() throws Exception {
		this.cloneTestRepo();
		final File local = new File("bin/test-httpd");
		Git g = Git.cloneRepository()
				.setProgressMonitor(new TextProgressMonitor())
				.setURI(this.getBaseURI().resolve("/proj/repo.git").toString())
				.setBare(false).setDirectory(local).call();
		try {
			assertTrue(local.exists());
			File newFile = new File(local, "README");
			List<String> actLines = Files.readLines(
					new File("test/repo/README"), Charsets.UTF_8);
			List<String> expLines = Files.readLines(newFile, Charsets.UTF_8);
			assertEquals(actLines, expLines);

			Files.write("aaaaa", new File(local, "test"), Charsets.UTF_8);
			g.commit().setMessage("ぐわわ…")
					.setAuthor("httpd-tester", "httpdtest@koshinuke.org")
					.call();
			g.tag().setName("hogehoge").setMessage("fugafuga").call();
			g.push().call();
		} finally {
			GitUtil.close(g);
		}

	}
}
