package org.koshinuke.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author taichi
 */
public class PropertiesConfiguration implements Configuration {

	Properties properties = new Properties();

	@Override
	public void configure(URL resource) throws IOException {
		try (InputStream in = resource.openStream()) {
			properties.load(in);
		}
	}

	@Override
	public File getRepositoryRoot() {
		File f = new File(this.properties.getProperty(REPO_ROOT));
		// TODO エラーチェック
		return f;
	}

}
