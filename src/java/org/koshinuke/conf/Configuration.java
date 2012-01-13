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

	String REPO_ROOT = "dir.repository";
	String TEMPORARY = "dir.temporary";
	String SYSTEM_IDENT_NAME = "system.ident.name";
	String SYSTEM_IDENT_MAIL = "system.ident.mail";
	String GIT_HOSTNAME = "git.hostname";

	// TODO 各デーモンがデフォルトポート以外を使用している際の対応。
	String GIT_SSH_PORTNO = "git.ssh.portno";
	String GIT_HTTPS_PORTNO = "git.https.portno";
	String GIT_GIT_PORTNO = "git.git.portno";

	void configure(URL resource) throws IOException;

	File getRepositoryRootDir();

	File getWorkingDir();

	PersonIdent getSystemIdent();

	String getGitHost();
}
