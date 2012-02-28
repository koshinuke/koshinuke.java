package org.koshinuke.service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.koshinuke.filter.AuthenticationFilter;
import org.koshinuke.model.Auth;
import org.koshinuke.util.ServletUtil;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("/login")
@Produces(MediaType.TEXT_HTML)
public class UserService {

	@GET
	public Viewable login(@Context HttpServletRequest req,
			@Context HttpServletResponse res) {
		if (AuthenticationFilter.isLoggedIn(req)) {
			ServletUtil.redirect(res, "/");
			return null;
		}
		return Auth.of("/login", req.getSession());
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Object login(@Context HttpServletRequest req,
			@FormParam("u") String u, @FormParam("p") String p,
			@FormParam("t") String t, @Context UriInfo info) {
		try {
			HttpSession session = req.getSession(false);
			if (ServletUtil.verifyCsrf(session, t)) {
				req.login(u, p);
				if (session != null) {
					session.invalidate();
				}
				AuthenticationFilter.setUserPrincipal(req);
				// HttpServletResponse#sendRedirectを使い、リダイレクト先としてコンテキストルートを指定すると、
				// このリクエストを送信する際にはまだHttpSessionが存在しない為に、
				// URLのセッションIDと/が隣合う不適切なLocationヘッダが生成されてしまうので、回避措置。
				// 尚、Set-Cookieヘッダーは正しく設定されるので、アプリケーションとしては適切に動作する。
				// see. org.eclipse.jetty.server.Response#encodeURL
				return Response
						.status(HttpServletResponse.SC_MOVED_TEMPORARILY)
						.location(info.getBaseUri()).build();
			}
		} catch (ServletException e) {
			// login failed
		}
		return Auth.of("/login", req.getSession());
	}
}
