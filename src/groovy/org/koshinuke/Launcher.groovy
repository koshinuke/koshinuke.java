package org.koshinuke

import java.io.File

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.SessionManager
import org.eclipse.jetty.webapp.WebAppContext

class Launcher {
	int portNo = 80
	def confDir = 'etc'
	def warURL = getClass().protectionDomain.codeSource.location.toExternalForm()
	def webDefaults = WebAppContext.WEB_DEFAULTS_XML

	def run() {
		def server = new Server(portNo)
		server.stopAtShutdown = true
		server.gracefulShutdown = 1000
		server.sendServerVersion = false

		System.properties['java.security.auth.login.config'] = new File(confDir,'jaas.conf').path
		server.addBean(new org.eclipse.jetty.plus.jaas.JAASLoginService('Koshinuke'))

		def web = new WebAppContext()
		web.contextPath = '/'
		web.extractWAR = false

		def s = 'sid'
		web.initParams[SessionManager.__SessionCookieProperty] = s
		web.initParams[SessionManager.__SessionIdPathParameterNameProperty] = s
		web.sessionHandler.sessionManager.httpOnly = true
		web.attributes.setAttribute "org.koshinuke.conf.Configuration", new File(confDir, 'koshinuke.properties').toURI().toURL()

		web.war = warURL
		web.defaultsDescriptor = webDefaults

		server.handler = web

		server.start()
		server.join()
	}
}
