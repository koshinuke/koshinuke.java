package org.koshinuke.jersey;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import com.google.common.collect.Maps;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.parseinfo.SoyFileInfo;
import com.google.template.soy.parseinfo.SoyTemplateInfo;
import com.google.template.soy.tofu.SoyTofu;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;

/**
 * @author taichi
 */
@Provider
public class SoyViewProcessor implements ViewProcessor<String> {

	@Context
	ThreadLocal<HttpServletResponse> responseInvoker;

	Map<String, SoyTemplateInfo> templates = Maps.newHashMap();

	SoyTofu tofu;

	@Inject
	public SoyViewProcessor(ServletContext sc, Set<SoyFileInfo> list)
			throws IOException {
		SoyFileSet.Builder b = new SoyFileSet.Builder();
		for (SoyFileInfo f : list) {
			String path = "/WEB-INF/soy/" + f.getFileName();
			URL url = sc.getResource(path);
			b.add(url);
			for (SoyTemplateInfo t : f.getTemplates()) {
				this.templates.put("/" + t.getName(), t);
			}
		}
		this.tofu = b.build().compileToTofu();
	}

	@Override
	public String resolve(String name) {
		SoyTemplateInfo t = this.templates.get(name);
		if (t != null) {
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
		SoyTemplateInfo t = this.templates.get(name);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out,
				"UTF-8"));
		this.tofu.newRenderer(t).render(bw);
		bw.flush();
	}
}
