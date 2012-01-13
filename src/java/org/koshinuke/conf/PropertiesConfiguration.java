package org.koshinuke.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
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

	static final File REPO;
	static final File TEMP;
	static final String HOSTNAME;

	static {
		File root = new File(System.getProperty("java.io.tmpdir"), "koshinuke");
		REPO = new File(root, "bares");
		TEMP = new File(root, "working");
		String host = "localhost";
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		HOSTNAME = host;
	}

	Properties properties = new Properties();

	File rootDir;
	File tempDir;

	@Override
	public void configure(URL resource) throws IOException {
		try (InputStream in = resource.openStream()) {
			this.properties.load(in);
		}
		this.rootDir = this.dir(REPO_ROOT, REPO);
		this.tempDir = this.dir(TEMPORARY, TEMP);
	}

	protected File dir(String key, File DEF) {
		String path = this.properties.getProperty(key);
		if (StringUtils.isEmptyOrNull(path)) {
			return DEF;
		}
		File f = new File(path).getAbsoluteFile();
		if (f.canWrite() == false) {
			return DEF;
		}
		return f;
	}

	@Override
	public File getRepositoryRootDir() {
		return this.rootDir;
	}

	@Override
	public File getWorkingDir() {
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
