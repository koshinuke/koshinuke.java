package org.koshinuke;

import java.io.File;

import org.eclipse.jetty.plus.jaas.JAASLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.koshinuke.conf.Configuration;
import org.koshinuke.util.RandomUtil;

public class Main {

	protected static Server start() throws Exception {
		Server server = new Server(9998);
		server.setSendServerVersion(false);
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
		String s = RandomUtil.nextString(6);
		sch.setInitParameter(SessionManager.__SessionCookieProperty, s);
		sch.setInitParameter(
				SessionManager.__SessionIdPathParameterNameProperty, s);
		AbstractSessionManager asm = (AbstractSessionManager) sch
				.getSessionHandler().getSessionManager();
		asm.setHttpOnly(true);
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
