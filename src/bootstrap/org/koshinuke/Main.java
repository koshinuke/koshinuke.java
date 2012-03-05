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
		ProtectionDomain protectionDomain = Main.class.getProtectionDomain();
		URL location = protectionDomain.getCodeSource().getLocation();
		webAppContext.setWar(location.toExternalForm());
	}

	public static void main(String[] args) throws Exception {
		Main me = new Main();
		Server server = me.start();
		server.join();
	}
}
