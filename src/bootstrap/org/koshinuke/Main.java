package org.koshinuke;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author taichi
 */
public class Main extends Launcher {

	@Override
	protected void initialize(WebAppContext webAppContext) {
		ProtectionDomain domain = Main.class.getProtectionDomain();
		URL location = domain.getCodeSource().getLocation();
		webAppContext.setWar(location.toExternalForm());
	}

	protected void prepare() {
		File etc = new File("etc");
		if (etc.exists() == false) {
			etc.mkdir();
			ClassLoader cl = Main.class.getClassLoader();
			String[] ary = { "jaas.conf", "koshinuke.properties",
					"login.properties" };
			for (String s : ary) {
				URL url = cl.getResource("WEB-INF/etc/" + s);
				try (InputStream in = url.openStream();
						FileOutputStream out = new FileOutputStream(new File(
								etc, s))) {
					IO.copy(in, out);
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Main me = new Main();
		me.prepare();
		Server server = me.start();
		server.join();
	}
}
