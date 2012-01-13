package org.koshinuke.filter;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.util.StringUtils;
import org.koshinuke.util.ServletUtil;

/**
 * @author taichi
 */
public class CsrfFilter implements Filter {

	static final Pattern METHODS = Pattern.compile("GET|HEAD",
			Pattern.CASE_INSENSITIVE);
	static final String KOSHINUKE = "X-KoshiNuke";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		if (verify(req, res)) {
			chain.doFilter(request, response);
		} else {
			res.sendRedirect("/");
		}
	}

	protected boolean verify(HttpServletRequest request,
			HttpServletResponse response) {
		String method = request.getMethod();
		return (StringUtils.isEmptyOrNull(method) == false)
				&& (METHODS.matcher(method).matches() || ServletUtil
						.verifyCsrf(request.getSession(false),
								request.getHeader(KOSHINUKE)));
	}

	@Override
	public void destroy() {
	}
}
