package org.koshinuke;

import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author taichi
 */
public class Debug extends Launcher {

	@Override
	protected void initialize(WebAppContext webAppContext) {
		webAppContext.setWar("src/webapp");
		webAppContext.setDefaultsDescriptor("etc/webdefault.xml");
	}

	public static void main(String[] args) throws Exception {
		new Debug().start().join();
	}
}
