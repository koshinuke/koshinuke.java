package org.koshinuke.service;

import java.security.Principal;

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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.koshinuke.filter.AuthenticationFilter;
import org.koshinuke.soy.LoginSoyInfo;
import org.koshinuke.soy.SoyTemplatesModule;
import org.koshinuke.util.ServletUtil;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.core.header.OutBoundHeaders;
import com.sun.jersey.core.spi.factory.ResponseImpl;

@Path("login")
@Produces(MediaType.TEXT_HTML)
public class LoginService {

	@GET
	public Viewable login(@Context HttpServletRequest req,
			@Context HttpServletResponse res) {
		if (AuthenticationFilter.isLoggedIn(req)) {
			ServletUtil.redirect(res, "/");
			return null;
		}
		return SoyTemplatesModule.of(LoginSoyInfo.LOGINFORM);
	}

	static class Redirect extends ResponseImpl {
		public Redirect(String location) {
			super(HttpServletResponse.SC_MOVED_TEMPORARILY, make(location),
					null, null);
		}

		private static OutBoundHeaders make(String location) {
			OutBoundHeaders header = new OutBoundHeaders();
			header.add(HttpHeaders.LOCATION, location);
			return header;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Object login(@Context HttpServletRequest req,
			@Context HttpServletResponse res, @FormParam("u") String u,
			@FormParam("p") String p) {
		try {
			req.login(u, p);
			HttpSession session = req.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			session = req.getSession(true);
			Principal principal = req.getUserPrincipal();
			session.setAttribute(AuthenticationFilter.AUTH, principal);
			// HttpServletResponse#sendRedirectを使い、
			// リダイレクト先としてコンテキストルートを指定すると、
			// このリクエストを送信する際にはまだHttpSessionが存在しない為に、
			// URLのセッションIDと/が隣合う不適切なLocationヘッダが生成されてしまうので、回避措置。
			// 尚、Set-Cookieヘッダーは正しく設定されるので、アプリケーションとしては適切に動作する。
			// see. org.eclipse.jetty.server.Response#encodeURL
			StringBuffer stb = req.getRequestURL();
			return new Redirect(stb.substring(0, stb.indexOf("login") - 1));
		} catch (ServletException e) {
			// login failed
		}
		return SoyTemplatesModule.of(LoginSoyInfo.LOGINFORM);
	}
}
