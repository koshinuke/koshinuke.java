package org.koshinuke

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.SessionManager
import org.eclipse.jetty.webapp.WebAppContext

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
			new File(etc, n).withOutputStream { dest ->
				def buffer = new BufferedOutputStream(dest)
				buffer << src
			}
		}
	}
}


def server = new Server(80)
server.stopAtShutdown = true
server.gracefulShutdown = 1000
server.sendServerVersion = false

System.properties['java.security.auth.login.config'] = 'etc/jaas.conf'
server.addBean(new org.eclipse.jetty.plus.jaas.JAASLoginService('Koshinuke'))

def web = new WebAppContext()
web.contextPath = '/'

def s = 'sid'
web.initParams[SessionManager.__SessionCookieProperty] = s
web.initParams[SessionManager.__SessionIdPathParameterNameProperty] = s
web.sessionHandler.sessionManager.httpOnly = true
web.attributes.setAttribute "org.koshinuke.conf.Configuration", new File('etc/koshinuke.properties').toURI().toURL()

web.war = warURL
web.defaultsDescriptor = webDefaults

server.handler = web

server.start()
server.join()
