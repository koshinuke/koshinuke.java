package org.koshinuke.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;

import javax.ws.rs.core.Application;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.util.FileUtils;
import org.junit.BeforeClass;
import org.koshinuke.App;
import org.koshinuke.conf.Configuration;
import org.koshinuke.jersey.TestConfigurationtProvider;
import org.koshinuke.util.GitUtil;

import com.google.common.base.Function;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

/**
 * @author taichi
 */
public abstract class KoshinukeTest extends JerseyTest {

	protected static Configuration config;

	@Override
	protected TestContainerFactory getTestContainerFactory()
			throws TestContainerException {
		return new JettyTestContainerFactory();
	}

	@Override
	protected AppDescriptor configure() {
		AppDescriptor ad = new SimpleAppDescriptor.Builder(
				this.getApplicationClass()).build();
		ad.getClientConfig().getSingletons().add(App.makeJsonProvider());
		return ad;
	}

	protected abstract Class<? extends Application> getApplicationClass();

	@BeforeClass
	public static void loadConfig() throws Exception {
		config = new TestConfigurationtProvider().getValue();
	}

	protected File cloneTestRepo() throws Exception {
		Path path = config.getRepositoryRootDir().resolve("proj/repo");
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

	public static void deleteDirs() throws Exception {
		File bin = new File("bin");
		for (File test : bin.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("test");
			}
		})) {
			FileUtils
					.delete(test, FileUtils.RECURSIVE | FileUtils.SKIP_MISSING);
		}
	}
}
