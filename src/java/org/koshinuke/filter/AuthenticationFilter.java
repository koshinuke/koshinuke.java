package org.koshinuke.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author taichi
 */
@WebFilter(urlPatterns = "/*")
public class AuthenticationFilter implements Filter {

	public static final String AUTH = AuthenticationFilter.class.getName()
			+ ".auth";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		System.out.println(request.getContextPath());
		System.out.println(request.getRequestURI());

		if (request.getContextPath().equals(request.getRequestURI())
				|| isLoggedIn(request)) {
			chain.doFilter(request, response);
		} else {
			response.sendRedirect("/");
		}
	}

	public static boolean isLoggedIn(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			return session.getAttribute(AUTH) != null;
		}
		return false;
	}

	@Override
	public void destroy() {
	}

}
