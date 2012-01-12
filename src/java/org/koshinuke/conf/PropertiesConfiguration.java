package org.koshinuke.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.jgit.lib.PersonIdent;

/**
 * @author taichi
 */
public class PropertiesConfiguration implements Configuration {

	Properties properties = new Properties();

	@Override
	public void configure(URL resource) throws IOException {
		try (InputStream in = resource.openStream()) {
			this.properties.load(in);
		}
	}

	@Override
	public File getRepositoryRootDir() {
		return new File(this.properties.getProperty(REPO_ROOT, "repos/bares"))
				.getAbsoluteFile();
	}

	@Override
	public File getWorkingDir() {
		return new File(this.properties.getProperty(WORKING, "repos/workings"))
				.getAbsoluteFile();
	}

	@Override
	public PersonIdent getSystemIdent() {
		String name = this.properties.getProperty(SYSTEM_IDENT_NAME,
				"koshinuke");
		String mail = this.properties.getProperty(SYSTEM_IDENT_MAIL,
				"koshinuke@koshinuke.org");
		return new PersonIdent(name, mail);
	}
}
