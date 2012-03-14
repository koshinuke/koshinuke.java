package org.koshinuke

import groovy.lang.GroovyClassLoader
import groovy.lang.GroovyCodeSource
import groovy.lang.GroovyObject

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.eclipse.jetty.webapp.WebAppContext

def runScript = { args ->
	if (0 < args.length) {
		File f = new File(args[0])
		if (f.exists() && f.canRead()) {
			def cc = new CompilerConfiguration()
			def ic = new ImportCustomizer()
			ic.addStarImports("org.eclipse.jetty.server", "org.eclipse.jetty.webapp")
			cc.addCompilationCustomizers(ic)
			def gcl = new GroovyClassLoader(getClass().getClassLoader(), cc, true)
			GroovyObject go = gcl.parseClass(new GroovyCodeSource(f)).newInstance()
			go.invokeMethod("run", args)
			return false
		}
	}
	return true
}

if(runScript(args)) {
	Launcher l = new Launcher()
	l.warURL = getClass().protectionDomain.codeSource.location.toExternalForm()
	l.webDefaults = WebAppContext.WEB_DEFAULTS_XML
	l.run()
}

