package org.koshinuke.jersey;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.koshinuke.util.Streams;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;

/**
 * @author taichi
 */
public class StaticViewProcessor implements ViewProcessor<String> {

	@Context
	ServletContext servletContext;

	@Context
	ThreadLocal<HttpServletResponse> responseInvoker;

	@Override
	public String resolve(String name) {
		String path = "/WEB-INF/static/" + name + ".html";
		try {
			if (this.servletContext.getResource(path) != null) {
				return path;
			}
		} catch (MalformedURLException e) {
		}
		return null;
	}

	@Override
	public void writeTo(String t, Viewable viewable, OutputStream out)
			throws IOException {
		InputStream in = null;
		try {
			HttpServletResponse response = responseInvoker.get();
			response.setHeader(HttpHeaders.CACHE_CONTROL, "No-cache");
			response.setDateHeader(HttpHeaders.EXPIRES, 1);
			in = this.servletContext.getResourceAsStream(t);
			Streams.copy(in, out);
			out.flush();
		} finally {
			Streams.close(in);
		}
	}
}
