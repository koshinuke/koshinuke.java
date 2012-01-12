package org.koshinuke.filter;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.koshinuke.model.DefaultKoshinukePrincipal;
import org.koshinuke.model.KoshinukePrincipal;

/**
 * @author taichi
 */
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
		if ("/login".equals(request.getRequestURI()) || isLoggedIn(request)) {
			chain.doFilter(request, response);
		} else {
			response.sendRedirect("/login");
		}
	}

	public static boolean isLoggedIn(HttpServletRequest request) {
		return getUserPrincipal(request) != null;
	}

	public static KoshinukePrincipal getUserPrincipal(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			return (KoshinukePrincipal) session.getAttribute(AUTH);
		}
		return null;
	}

	public static void setUserPrincipal(HttpServletRequest req) {
		HttpSession session = req.getSession(true);
		Principal principal = req.getUserPrincipal();
		session.setAttribute(AuthenticationFilter.AUTH,
				new DefaultKoshinukePrincipal(principal));
	}

	@Override
	public void destroy() {
	}

}
