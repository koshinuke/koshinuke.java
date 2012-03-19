package org.koshinuke.conf;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.util.StringUtils;

/**
 * @author taichi
 */
public class PropertiesConfiguration implements Configuration {

	static final Logger LOG = Logger.getLogger(PropertiesConfiguration.class
			.getName());

	static final Path REPO;
	static final Path TEMP;
	static final String HOSTNAME;

	static {
		FileSystem fs = FileSystems.getDefault();
		Path root = fs.getPath(System.getProperty("java.io.tmpdir"),
				"koshinuke");
		REPO = root.resolve("pares");
		TEMP = root.resolve("temp");
		String host = "localhost";
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		HOSTNAME = host;
	}

	Properties properties = new Properties();

	Path rootDir;
	Path tempDir;

	@Override
	public void configure(URL resource) throws IOException {
		try (InputStream in = resource.openStream()) {
			this.properties.load(in);
		}
		FileSystem fs = FileSystems.getDefault();
		this.rootDir = this.dir(fs, REPO_ROOT, REPO);
		this.tempDir = this.dir(fs, TEMPORARY, TEMP);
	}

	protected Path dir(FileSystem fs, String key, Path DEF) {
		String path = this.properties.getProperty(key);
		if (StringUtils.isEmptyOrNull(path)) {
			return DEF;
		}
		Path p = fs.getPath(path);
		if (Files.exists(p) == false && p.toFile().mkdirs() == false) {
			throw new IllegalStateException();
		}
		return p.toAbsolutePath();
	}

	@Override
	public Path getRepositoryRootDir() {
		return this.rootDir;
	}

	@Override
	public Path getWorkingDir() {
		return this.tempDir;
	}

	@Override
	public PersonIdent getSystemIdent() {
		String name = this.properties.getProperty(SYSTEM_IDENT_NAME,
				"koshinuke");
		String mail = this.properties.getProperty(SYSTEM_IDENT_MAIL,
				"koshinuke@koshinuke.org");
		return new PersonIdent(name, mail);
	}

	@Override
	public String getGitHost() {
		return this.properties.getProperty(GIT_HOSTNAME, HOSTNAME);
	}
}
