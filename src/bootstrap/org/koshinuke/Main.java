package org.koshinuke;

import java.io.File;
import java.security.SecureRandom;

import org.eclipse.jetty.plus.jaas.JAASLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.koshinuke.conf.Configuration;

public class Main {

	protected static Server start() throws Exception {
		Server server = new Server(9998);
		ServletContextHandler sch = new WebAppContext("src/webapp", "/");
		sessionCookieSecured(sch);
		sch.setAttribute(Configuration.NAME, new File(
				"etc/koshinuke.properties").toURI().toURL());
		server.setHandler(sch);
		SecurityHandler secure = securitySettings(sch);
		server.start();
		secure.start();
		return server;
	}

	protected static void sessionCookieSecured(ServletContextHandler sch) {
		String s = newSessionKey();
		sch.setInitParameter(SessionManager.__SessionCookieProperty, s);
		sch.setInitParameter(
				SessionManager.__SessionIdPathParameterNameProperty, s);
		AbstractSessionManager asm = (AbstractSessionManager) sch
				.getSessionHandler().getSessionManager();
		asm.setHttpOnly(true);
	}

	static final char[] CANDIDATE = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
			'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
			'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
			'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9' };

	protected static String newSessionKey() {
		SecureRandom random = new SecureRandom(SecureRandom.getSeed(512));
		StringBuilder stb = new StringBuilder(8);
		int range = CANDIDATE.length - 1;
		for (int i = 0; i < 8; i++) {
			stb.append(CANDIDATE[random.nextInt(range)]);
		}
		return stb.toString();
	}

	protected static SecurityHandler securitySettings(ServletContextHandler sch)
			throws Exception {
		System.setProperty("java.security.auth.login.config", "etc/jaas.conf");
		SecurityHandler secure = sch.getSecurityHandler();
		secure.setAuthMethod("FORM");
		secure.setRealmName("Login");
		JAASLoginService ls = new JAASLoginService(secure.getRealmName());
		ls.start();
		secure.setLoginService(ls);
		return secure;
	}

	public static void main(String[] args) throws Exception {
		Server server = start();

		System.in.read();
		server.stop();
	}

}
