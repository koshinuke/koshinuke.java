package org.koshinuke

import groovy.lang.GroovyClassLoader
import groovy.lang.GroovyCodeSource


def runScript = { args ->
	if (0 < args.length) {
		File f = new File(args[0])
		if (f.exists() && f.canRead()) {
			def gcl = new GroovyClassLoader()
			def script = gcl.parseClass(new GroovyCodeSource(f)).newInstance()
			script.run(f, args)
			return false
		}
	}
	return true
}

def copySettings = {
	def etc = new File('etc')
	if(etc.exists() == false) {
		etc.mkdirs()
		def cl = getClass().classLoader
		[
			'jaas.conf',
			'koshinuke.properties',
			'login.properties'
		].each { n ->
			cl.getResource("WEB-INF/etc/$n").withInputStream { src ->
				new File(etc, n).withOutputStream {
					def buffer = new BufferedOutputStream(it)
					buffer << src
				}
			}
		}
	}
}

copySettings()

if(runScript(args)) {
	new Launcher().run()
}
