package org.koshinuke;

import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author taichi
 */
public class Main extends Launcher {

	@Override
	protected void initialize(WebAppContext webAppContext) {
		ProtectionDomain domain = Main.class.getProtectionDomain();
		ClassLoader loader = domain.getClassLoader();
		URL war = loader.getResource("koshinuke.java.war");
		System.out.println(war);
		webAppContext.setWar(war.toExternalForm());
	}

	public static void main(String[] args) throws Exception {
		// TODO modify bootstrap classpath
		// TODO add default etc files if it doesn't exists
		Main me = new Main();
		Server server = me.start();
		server.join();
	}
}
