package org.koshinuke;

import java.io.File;

import org.eclipse.jetty.plus.jaas.JAASLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author taichi
 */
public abstract class Launcher {

	protected abstract void initialize(WebAppContext webAppContext);

	protected Server start() throws Exception {
		Server server = new Server(80);
		server.setSendServerVersion(false);
		this.securitySettings(server);
		WebAppContext sch = new WebAppContext();
		sch.setContextPath("/");
		this.sessionCookieSecured(sch);
		sch.setAttribute("org.koshinuke.conf.Configuration", new File(
				"etc/koshinuke.properties").toURI().toURL());
		server.setHandler(sch);

		this.initialize(sch);

		Runtime.getRuntime().addShutdownHook(
				new Thread(new ShutdownHook(server)));

		server.start();
		return server;
	}

	protected void sessionCookieSecured(ServletContextHandler sch) {
		String s = "sid";
		sch.setInitParameter(SessionManager.__SessionCookieProperty, s);
		sch.setInitParameter(
				SessionManager.__SessionIdPathParameterNameProperty, s);
		AbstractSessionManager asm = (AbstractSessionManager) sch
				.getSessionHandler().getSessionManager();
		asm.setHttpOnly(true);
	}

	protected void securitySettings(Server server) throws Exception {
		String key = "java.security.auth.login.config";
		String s = System.getProperty(key);
		if (s == null || s.isEmpty()) {
			System.setProperty(key, "etc/jaas.conf");
		}
		server.addBean(new JAASLoginService("Koshinuke"));
	}

	static class ShutdownHook implements Runnable {
		final Server server;

		public ShutdownHook(Server server) {
			this.server = server;
		}

		@Override
		public void run() {
			try {
				this.server.stop();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}

}
