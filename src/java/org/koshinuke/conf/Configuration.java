package org.koshinuke.conf;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author taichi
 */
public interface Configuration {

	String NAME = Configuration.class.getName();

	String REPO_ROOT = "repository_root_dir";

	void configure(URL resource) throws IOException;

	File getRepositoryRoot();
}
