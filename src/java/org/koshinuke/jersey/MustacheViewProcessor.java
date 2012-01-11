package org.koshinuke.jersey;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import com.sampullara.mustache.Mustache;
import com.sampullara.mustache.MustacheBuilder;
import com.sampullara.mustache.MustacheContext;
import com.sampullara.mustache.MustacheException;
import com.sampullara.mustache.Scope;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.spi.template.ViewProcessor;

/**
 * @author taichi
 */
public class MustacheViewProcessor implements ViewProcessor<String> {

	@Context
	ThreadLocal<HttpServletResponse> responseInvoker;

	Map<String, Mustache> templates = new HashMap<>();

	public MustacheViewProcessor(final @Context ServletContext sc)
			throws MustacheException {

		MustacheBuilder builder = new MustacheBuilder(new MustacheContext() {
			@Override
			public BufferedReader getReader(String name)
					throws MustacheException {
				String path = "/WEB-INF/mustache" + name + ".html";

				return new BufferedReader(new InputStreamReader(
						sc.getResourceAsStream(path), ReaderWriter.UTF8));
			}
		});
		String[] ary = { "/login", "/repos" };
		for (String s : ary) {
			this.templates.put(s, builder.parseFile(s));
		}
	}

	@Override
	public String resolve(String name) {
		if (this.templates.containsKey(name)) {
			return name;
		}
		return null;
	}

	@Override
	public void writeTo(String name, Viewable viewable, OutputStream out)
			throws IOException {
		HttpServletResponse response = responseInvoker.get();
		response.setHeader(HttpHeaders.CACHE_CONTROL, "No-cache");
		response.setDateHeader(HttpHeaders.EXPIRES, 1);
		try {
			Mustache t = this.templates.get(name);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out,
					ReaderWriter.UTF8));
			t.execute(bw, new Scope(viewable.getModel()));
			bw.flush();
		} catch (MustacheException e) {
			throw new IOException(e);
		}
	}
}
