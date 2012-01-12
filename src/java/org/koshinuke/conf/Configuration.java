package org.koshinuke.conf;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.jgit.lib.PersonIdent;

/**
 * @author taichi
 */
public interface Configuration {

	String NAME = Configuration.class.getName();

	String REPO_ROOT = "repository_root_dir";
	String WORKING = "working_dir";
	String SYSTEM_IDENT_NAME = "system.ident.name";
	String SYSTEM_IDENT_MAIL = "system.ident.mail";

	void configure(URL resource) throws IOException;

	File getRepositoryRootDir();

	File getWorkingDir();

	PersonIdent getSystemIdent();
}
